/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.rpc.Status;
import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpMethod;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.metrics.ResourceMetricsMarshaler;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.tls.HeldCertificate;
import okio.Buffer;
import okio.GzipSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class OtlpHttpMetricExporterTest {

  private static final MediaType APPLICATION_PROTOBUF =
      MediaType.create("application", "x-protobuf");
  private static final HeldCertificate HELD_CERTIFICATE;
  private static final String canonicalHostName;

  static {
    try {
      canonicalHostName = InetAddress.getByName("localhost").getCanonicalHostName();
      HELD_CERTIFICATE =
          new HeldCertificate.Builder()
              .commonName("localhost")
              .addSubjectAlternativeName(canonicalHostName)
              .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Error building certificate.", e);
    }
  }

  @RegisterExtension
  static MockWebServerExtension server =
      new MockWebServerExtension() {
        @Override
        protected void configureServer(ServerBuilder sb) {
          sb.tls(HELD_CERTIFICATE.keyPair().getPrivate(), HELD_CERTIFICATE.certificate());
        }
      };

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpHttpMetricExporter.class);

  private OtlpHttpMetricExporterBuilder builder;

  @BeforeEach
  void setup() {
    builder =
        OtlpHttpMetricExporter.builder()
            .setEndpoint("https://" + canonicalHostName + ":" + server.httpsPort() + "/v1/metrics")
            .addHeader("foo", "bar")
            .setTrustedCertificates(
                HELD_CERTIFICATE.certificatePem().getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void validConfig() {
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setTimeout(0, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setTimeout(Duration.ofMillis(0)))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setTimeout(10, TimeUnit.MILLISECONDS))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setTimeout(Duration.ofMillis(10)))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpHttpMetricExporter.builder().setEndpoint("http://localhost:4318"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setEndpoint("http://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setEndpoint("https://localhost"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setEndpoint("http://foo:bar@localhost"))
        .doesNotThrowAnyException();

    assertThatCode(() -> OtlpHttpMetricExporter.builder().setCompression("gzip"))
        .doesNotThrowAnyException();
    assertThatCode(() -> OtlpHttpMetricExporter.builder().setCompression("none"))
        .doesNotThrowAnyException();

    assertThatCode(
            () -> OtlpHttpMetricExporter.builder().addHeader("foo", "bar").addHeader("baz", "qux"))
        .doesNotThrowAnyException();

    assertThatCode(
            () ->
                OtlpHttpMetricExporter.builder()
                    .setTrustedCertificates("foobar".getBytes(StandardCharsets.UTF_8)))
        .doesNotThrowAnyException();
  }

  @Test
  @SuppressWarnings("PreferJavaTimeOverload")
  void invalidConfig() {
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> OtlpHttpMetricExporter.builder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }

  @Test
  void testExportUncompressed() {
    server.enqueue(successResponse());
    OtlpHttpMetricExporter exporter = builder.build();

    ExportMetricsServiceRequest payload =
        exportAndAssertResult(exporter, /* expectedResult= */ true);
    AggregatedHttpRequest request = server.takeRequest().request();
    assertRequestCommon(request);
    assertThat(parseRequestBody(request.content().array())).isEqualTo(payload);
  }

  @Test
  void testExportGzipCompressed() {
    server.enqueue(successResponse());
    OtlpHttpMetricExporter exporter = builder.setCompression("gzip").build();

    ExportMetricsServiceRequest payload =
        exportAndAssertResult(exporter, /* expectedResult= */ true);
    AggregatedHttpRequest request = server.takeRequest().request();
    assertRequestCommon(request);
    assertThat(request.headers().get("Content-Encoding")).isEqualTo("gzip");
    assertThat(parseRequestBody(gzipDecompress(request.content().array()))).isEqualTo(payload);
  }

  @Test
  void testExport_flush() {
    OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder().build();
    try {
      assertThat(exporter.flush().isSuccess()).isTrue();
    } finally {
      exporter.shutdown();
    }
  }

  private static void assertRequestCommon(AggregatedHttpRequest request) {
    assertThat(request.method()).isEqualTo(HttpMethod.POST);
    assertThat(request.path()).isEqualTo("/v1/metrics");
    assertThat(request.headers().get("foo")).isEqualTo("bar");
    assertThat(request.headers().get("Content-Type")).isEqualTo(APPLICATION_PROTOBUF.toString());
  }

  private static ExportMetricsServiceRequest parseRequestBody(byte[] bytes) {
    try {
      return ExportMetricsServiceRequest.parseFrom(bytes);
    } catch (InvalidProtocolBufferException e) {
      throw new IllegalStateException("Unable to parse Protobuf request body.", e);
    }
  }

  private static byte[] gzipDecompress(byte[] bytes) {
    try {
      Buffer result = new Buffer();
      GzipSource source = new GzipSource(new Buffer().write(bytes));
      while (source.read(result, Integer.MAX_VALUE) != -1) {}
      return result.readByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to decompress payload.", e);
    }
  }

  @Test
  void testServerError() {
    server.enqueue(
        buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            Status.newBuilder().setMessage("Server error!").build()));
    OtlpHttpMetricExporter exporter = builder.build();

    exportAndAssertResult(exporter, /* expectedResult= */ false);
    LoggingEvent log =
        logs.assertContains(
            "Failed to export metrics. Server responded with HTTP status code 500. Error message: Server error!");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  @Test
  void testServerErrorParseError() {
    server.enqueue(
        HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, APPLICATION_PROTOBUF, "Server error!"));
    OtlpHttpMetricExporter exporter = builder.build();

    exportAndAssertResult(exporter, /* expectedResult= */ false);
    LoggingEvent log =
        logs.assertContains(
            "Failed to export metrics. Server responded with HTTP status code 500. Error message: Unable to parse response body, HTTP status message:");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }

  private static ExportMetricsServiceRequest exportAndAssertResult(
      OtlpHttpMetricExporter otlpHttpMetricExporter, boolean expectedResult) {
    List<MetricData> metrics = Collections.singletonList(generateFakeMetric());
    CompletableResultCode resultCode = otlpHttpMetricExporter.export(metrics);
    resultCode.join(10, TimeUnit.SECONDS);
    assertThat(resultCode.isSuccess()).isEqualTo(expectedResult);
    List<ResourceMetrics> resourceMetrics =
        Arrays.stream(ResourceMetricsMarshaler.create(metrics))
            .map(
                marshaler -> {
                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  try {
                    marshaler.writeBinaryTo(bos);
                    return ResourceMetrics.parseFrom(bos.toByteArray());
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                })
            .collect(Collectors.toList());
    return ExportMetricsServiceRequest.newBuilder().addAllResourceMetrics(resourceMetrics).build();
  }

  private static HttpResponse successResponse() {
    ExportMetricsServiceResponse exportMetricsServiceResponse =
        ExportMetricsServiceResponse.newBuilder().build();
    return buildResponse(HttpStatus.OK, exportMetricsServiceResponse);
  }

  private static <T extends Message> HttpResponse buildResponse(HttpStatus httpStatus, T message) {
    return HttpResponse.of(httpStatus, APPLICATION_PROTOBUF, message.toByteArray());
  }

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return MetricDataBuilder.createLongSum(
        Resource.empty(),
        InstrumentationLibraryInfo.empty(),
        "name",
        "description",
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                LongPointData.create(startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }
}
