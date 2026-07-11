/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.internal.ComponentId;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class HttpExporterTest {

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(HttpExporter.class);

  @ParameterizedTest
  @EnumSource
  @SuppressLogger(HttpExporter.class)
  void testInternalTelemetry(StandardComponentId.ExporterType exporterType) {
    String signalMetricPrefix;
    String expectedUnit;
    switch (exporterType.signal()) {
      case SPAN:
        signalMetricPrefix = "otel.sdk.exporter.span.";
        expectedUnit = "{span}";
        break;
      case LOG:
        signalMetricPrefix = "otel.sdk.exporter.log.";
        expectedUnit = "{log_record}";
        break;
      case METRIC:
        signalMetricPrefix = "otel.sdk.exporter.metric_data_point.";
        expectedUnit = "{data_point}";
        break;
      case PROFILE:
        return; // Not yet supported
      default:
        throw new IllegalStateException();
    }

    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build()) {

      StandardComponentId id = ComponentId.generateLazy(exporterType);

      Attributes expectedAttributes =
          Attributes.builder()
              .put(SemConvAttributes.OTEL_COMPONENT_TYPE, id.getTypeName())
              .put(SemConvAttributes.OTEL_COMPONENT_NAME, id.getComponentName())
              .put(SemConvAttributes.SERVER_ADDRESS, "testing")
              .put(SemConvAttributes.SERVER_PORT, 1234)
              .build();

      HttpSender mockSender = Mockito.mock(HttpSender.class);
      Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
      MessageWriter emptyMessageWriter =
          new MessageWriter() {
            @Override
            public void writeMessage(OutputStream output) {}

            @Override
            public int getContentLength() {
              return 0;
            }
          };
      Mockito.when(mockMarshaller.toBinaryMessageWriter()).thenReturn(emptyMessageWriter);
      Mockito.when(mockMarshaller.toJsonMessageWriter()).thenReturn(emptyMessageWriter);

      HttpExporter exporter =
          new HttpExporter(
              id,
              mockSender,
              () -> meterProvider,
              InternalTelemetryVersion.LATEST,
              URI.create("http://testing:1234"),
              false,
              HttpExporterBuilder.DEFAULT_MAX_REQUEST_BODY_SIZE);

      doAnswer(
              invoc -> {
                Consumer<HttpResponse> onResponse = invoc.getArgument(1);

                assertThat(inMemoryMetrics.collectAllMetrics())
                    .hasSize(1)
                    .anySatisfy(
                        metric ->
                            assertThat(metric)
                                .hasName(signalMetricPrefix + "inflight")
                                .hasUnit(expectedUnit)
                                .hasLongSumSatisfying(
                                    ma ->
                                        ma.isNotMonotonic()
                                            .hasPointsSatisfying(
                                                pa ->
                                                    pa.hasAttributes(expectedAttributes)
                                                        .hasValue(42))));

                onResponse.accept(new FakeHttpResponse(200, "Ok"));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());

      exporter.export(mockMarshaller, 42);

      doAnswer(
              invoc -> {
                Consumer<HttpResponse> onResponse = invoc.getArgument(1);
                onResponse.accept(new FakeHttpResponse(404, "Not Found"));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());
      exporter.export(mockMarshaller, 15);

      doAnswer(
              invoc -> {
                Consumer<Throwable> onError = invoc.getArgument(2);
                onError.accept(new IOException("Computer says no"));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());
      exporter.export(mockMarshaller, 7);

      assertThat(inMemoryMetrics.collectAllMetrics())
          .hasSize(3)
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName(signalMetricPrefix + "inflight")
                      .hasUnit(expectedUnit)
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributes(expectedAttributes).hasValue(0))))
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName(signalMetricPrefix + "exported")
                      .hasUnit(expectedUnit)
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributes(expectedAttributes).hasValue(42),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(SemConvAttributes.ERROR_TYPE, "404")
                                                  .build())
                                          .hasValue(15),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.ERROR_TYPE,
                                                      "java.io.IOException")
                                                  .build())
                                          .hasValue(7))))
          .anySatisfy(
              metric ->
                  assertThat(metric)
                      .hasName("otel.sdk.exporter.operation.duration")
                      .hasUnit("s")
                      .hasHistogramSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.HTTP_RESPONSE_STATUS_CODE,
                                                      200)
                                                  .build())
                                          .hasBucketCounts(1),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(SemConvAttributes.ERROR_TYPE, "404")
                                                  .put(
                                                      SemConvAttributes.HTTP_RESPONSE_STATUS_CODE,
                                                      404)
                                                  .build())
                                          .hasBucketCounts(1),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.ERROR_TYPE,
                                                      "java.io.IOException")
                                                  .build())
                                          .hasBucketCounts(1))));
    }
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void export_httpJsonErrorBodyUsesBodyTextWithoutGrpcParseWarning() {
    HttpSender mockSender = Mockito.mock(HttpSender.class);
    Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
    MessageWriter emptyMessageWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) {}

          @Override
          public int getContentLength() {
            return 0;
          }
        };
    Mockito.when(mockMarshaller.toBinaryMessageWriter()).thenReturn(emptyMessageWriter);

    HttpExporter exporter =
        new HttpExporter(
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            URI.create("http://testing:1234"),
            false,
            HttpExporterBuilder.DEFAULT_MAX_REQUEST_BODY_SIZE);

    doAnswer(
            invoc -> {
              Consumer<HttpResponse> onResponse = invoc.getArgument(1);
              onResponse.accept(
                  new FakeHttpResponse(
                      500,
                      "Internal Server Error",
                      "{\"error\":\"grpc not supported\"}".getBytes(StandardCharsets.UTF_8)));
              return null;
            })
        .when(mockSender)
        .send(any(), any(), any());

    assertThat(exporter.export(mockMarshaller, 1).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();

    logs.assertContains("Response body: {\"error\":\"grpc not supported\"}");
    logs.assertDoesNotContain("Unable to parse response body");
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void export_requestBodyTooLargeFailsBeforeSend() {
    HttpSender mockSender = Mockito.mock(HttpSender.class);
    HttpExporter exporter =
        new HttpExporter(
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            URI.create("http://testing:1234"),
            false,
            1);

    Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
    MessageWriter messageWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) throws IOException {
            output.write(new byte[] {1, 2});
          }

          @Override
          public int getContentLength() {
            return 2;
          }
        };
    Mockito.when(mockMarshaller.toBinaryMessageWriter()).thenReturn(messageWriter);

    CompletableResultCode result = exporter.export(mockMarshaller, 1);

    Assertions.assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    Assertions.assertThat(result.getFailureThrowable())
        .hasMessageContaining(
            "Failed to export spans. Request body size 2 exceeded limit of 1 bytes");
    verifyNoInteractions(mockSender);
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void export_nullErrorBodyUsesMissingBodyMessage() {
    HttpSender mockSender = Mockito.mock(HttpSender.class);
    Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
    MessageWriter emptyMessageWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) {}

          @Override
          public int getContentLength() {
            return 0;
          }
        };
    Mockito.when(mockMarshaller.toBinaryMessageWriter()).thenReturn(emptyMessageWriter);
    HttpExporter exporter =
        new HttpExporter(
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            URI.create("http://testing:1234"),
            false,
            HttpExporterBuilder.DEFAULT_MAX_REQUEST_BODY_SIZE);

    doAnswer(
            invoc -> {
              Consumer<HttpResponse> onResponse = invoc.getArgument(1);
              onResponse.accept(new FakeHttpResponse(500, "Internal Server Error", null));
              return null;
            })
        .when(mockSender)
        .send(any(), any(), any());

    assertThat(exporter.export(mockMarshaller, 1).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();

    logs.assertContains("Response body missing, HTTP status message: Internal Server Error");
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void export_whitespaceErrorBodyFallsBackToStatusMessage() {
    HttpSender mockSender = Mockito.mock(HttpSender.class);
    Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
    MessageWriter emptyMessageWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) {}

          @Override
          public int getContentLength() {
            return 0;
          }
        };
    Mockito.when(mockMarshaller.toBinaryMessageWriter()).thenReturn(emptyMessageWriter);
    HttpExporter exporter =
        new HttpExporter(
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            URI.create("http://testing:1234"),
            false,
            HttpExporterBuilder.DEFAULT_MAX_REQUEST_BODY_SIZE);

    doAnswer(
            invoc -> {
              Consumer<HttpResponse> onResponse = invoc.getArgument(1);
              onResponse.accept(
                  new FakeHttpResponse(
                      500, "Internal Server Error", "   ".getBytes(StandardCharsets.UTF_8)));
              return null;
            })
        .when(mockSender)
        .send(any(), any(), any());

    assertThat(exporter.export(mockMarshaller, 1).join(10, TimeUnit.SECONDS).isSuccess()).isFalse();

    logs.assertContains("HTTP status message: Internal Server Error");
    logs.assertDoesNotContain("Response body:");
  }

  @Test
  @SuppressLogger(HttpExporter.class)
  void export_unknownRequestBodyTooLargeFailsBeforeSend() {
    HttpSender mockSender = Mockito.mock(HttpSender.class);
    HttpExporter exporter =
        new HttpExporter(
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER),
            mockSender,
            MeterProvider::noop,
            InternalTelemetryVersion.LATEST,
            URI.create("http://testing:1234"),
            true,
            1);

    Marshaler mockMarshaller = Mockito.mock(Marshaler.class);
    MessageWriter messageWriter =
        new MessageWriter() {
          @Override
          public void writeMessage(OutputStream output) throws IOException {
            output.write(new byte[] {1, 2});
          }

          @Override
          public int getContentLength() {
            return -1;
          }
        };
    Mockito.when(mockMarshaller.toJsonMessageWriter()).thenReturn(messageWriter);

    CompletableResultCode result = exporter.export(mockMarshaller, 1);

    Assertions.assertThat(result.join(10, TimeUnit.SECONDS).isSuccess()).isFalse();
    Assertions.assertThat(result.getFailureThrowable())
        .hasMessageContaining(
            "Failed to export spans. Request body size 2 exceeded limit of 1 bytes");
    verifyNoInteractions(mockSender);
  }

  private static class FakeHttpResponse implements HttpResponse {

    final int statusCode;
    final String statusMessage;
    @Nullable final byte[] responseBody;

    FakeHttpResponse(int statusCode, String statusMessage) {
      this(statusCode, statusMessage, new byte[0]);
    }

    FakeHttpResponse(int statusCode, String statusMessage, @Nullable byte[] responseBody) {
      this.statusCode = statusCode;
      this.statusMessage = statusMessage;
      this.responseBody = responseBody;
    }

    @Override
    public int getStatusCode() {
      return statusCode;
    }

    @Override
    public String getStatusMessage() {
      return statusMessage;
    }

    @Override
    @Nullable
    public byte[] getResponseBody() {
      return responseBody;
    }
  }
}
