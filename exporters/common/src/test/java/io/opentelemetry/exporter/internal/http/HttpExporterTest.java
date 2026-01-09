/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.internal.StandardComponentId;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.IOException;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class HttpExporterTest {

  @Test
  void build_NoHttpSenderProvider() {
    assertThatThrownBy(
            () ->
                new HttpExporterBuilder<>(
                        StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER,
                        "http://localhost")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No HttpSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-jdk");
  }

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

      HttpExporter<Marshaler> exporter =
          new HttpExporter<Marshaler>(
              id,
              mockSender,
              () -> meterProvider,
              InternalTelemetryVersion.LATEST,
              "http://testing:1234");

      doAnswer(
              invoc -> {
                Consumer<HttpSender.Response> onResponse = invoc.getArgument(2);

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
          .send(any(), anyInt(), any(), any());

      exporter.export(mockMarshaller, 42);

      doAnswer(
              invoc -> {
                Consumer<HttpSender.Response> onResponse = invoc.getArgument(2);
                onResponse.accept(new FakeHttpResponse(404, "Not Found"));
                return null;
              })
          .when(mockSender)
          .send(any(), anyInt(), any(), any());
      exporter.export(mockMarshaller, 15);

      doAnswer(
              invoc -> {
                Consumer<Throwable> onError = invoc.getArgument(3);
                onError.accept(new IOException("Computer says no"));
                return null;
              })
          .when(mockSender)
          .send(any(), anyInt(), any(), any());
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

  private static class FakeHttpResponse implements HttpSender.Response {

    final int statusCode;
    final String statusMessage;

    FakeHttpResponse(int statusCode, String statusMessage) {
      this.statusCode = statusCode;
      this.statusMessage = statusMessage;
    }

    @Override
    public int statusCode() {
      return statusCode;
    }

    @Override
    public String statusMessage() {
      return statusMessage;
    }

    @Override
    public byte[] responseBody() throws IOException {
      return new byte[0];
    }
  }
}
