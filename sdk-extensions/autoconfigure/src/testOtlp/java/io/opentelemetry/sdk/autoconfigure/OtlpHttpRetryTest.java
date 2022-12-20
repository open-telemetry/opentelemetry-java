/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeLog;
import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeMetric;
import static io.opentelemetry.sdk.autoconfigure.OtlpHttpServerExtension.generateFakeSpan;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporter;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(OkHttpExporter.class)
class OtlpHttpRetryTest {

  private static final List<SpanData> SPAN_DATA = Lists.newArrayList(generateFakeSpan());
  private static final List<MetricData> METRIC_DATA = Lists.newArrayList(generateFakeMetric());
  private static final List<LogRecordData> LOG_RECORD_DATA = Lists.newArrayList(generateFakeLog());

  @RegisterExtension
  public static final OtlpHttpServerExtension server = new OtlpHttpServerExtension();

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void configureSpanExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.traces.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.traces.endpoint",
        "https://localhost:" + server.httpsPort() + "/v1/traces");
    props.put(
        "otel.exporter.otlp.traces.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    ConfigProperties properties = DefaultConfigProperties.createForTest(props);
    try (SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            SpanExporterConfiguration.spanExporterSpiManager(
                properties, OtlpHttpRetryTest.class.getClassLoader()))) {

      testRetryableStatusCodes(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
      testDefaultRetryPolicy(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
    }
  }

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void configureMetricExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.metrics.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.metrics.endpoint",
        "https://localhost:" + server.httpsPort() + "/v1/metrics");
    props.put(
        "otel.exporter.otlp.metrics.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    try (MetricExporter metricExporter =
        MetricExporterConfiguration.configureExporter(
            "otlp",
            MetricExporterConfiguration.metricExporterSpiManager(
                DefaultConfigProperties.createForTest(props),
                OtlpHttpRetryTest.class.getClassLoader()))) {

      testRetryableStatusCodes(
          () -> METRIC_DATA, metricExporter::export, server.metricRequests::size);
      testDefaultRetryPolicy(
          () -> METRIC_DATA, metricExporter::export, server.metricRequests::size);
    }
  }

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void configureLogRecordExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.logs.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.logs.endpoint", "https://localhost:" + server.httpsPort() + "/v1/logs");
    props.put(
        "otel.exporter.otlp.logs.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    try (LogRecordExporter logRecordExporter =
        LogRecordExporterConfiguration.configureExporter(
            "otlp",
            LogRecordExporterConfiguration.logRecordExporterSpiManager(
                DefaultConfigProperties.createForTest(props),
                OtlpHttpRetryTest.class.getClassLoader()))) {
      testRetryableStatusCodes(
          () -> LOG_RECORD_DATA, logRecordExporter::export, server.logRequests::size);
      testDefaultRetryPolicy(
          () -> LOG_RECORD_DATA, logRecordExporter::export, server.logRequests::size);
    }
  }

  private static <T> void testRetryableStatusCodes(
      Supplier<T> dataSupplier,
      Function<T, CompletableResultCode> exporter,
      Supplier<Integer> serverRequestCountSupplier) {

    List<Integer> statusCodes = Arrays.asList(200, 400, 401, 403, 429, 500, 501, 502, 503);

    for (Integer code : statusCodes) {
      server.reset();

      server.responses.add(HttpResponse.of(HttpStatus.valueOf(code)));
      server.responses.add(HttpResponse.of(HttpStatus.OK));

      CompletableResultCode resultCode =
          exporter.apply(dataSupplier.get()).join(10, TimeUnit.SECONDS);
      assertThat(resultCode.isDone())
          .as("Exporter didn't complete in time. Consider increasing join timeout?")
          .isTrue();

      boolean retryable = code != 200 && RetryUtil.retryableHttpResponseCodes().contains(code);
      boolean expectedResult = retryable || code == 200;
      assertThat(resultCode.isSuccess())
          .as(
              "status code %s should export %s",
              code, expectedResult ? "successfully" : "unsuccessfully")
          .isEqualTo(expectedResult);
      int expectedRequests = retryable ? 2 : 1;
      assertThat(serverRequestCountSupplier.get())
          .as("status code %s should make %s requests", code, expectedRequests)
          .isEqualTo(expectedRequests);
    }
  }

  private static <T> void testDefaultRetryPolicy(
      Supplier<T> dataSupplier,
      Function<T, CompletableResultCode> exporter,
      Supplier<Integer> serverRequestCountSupplier) {
    server.reset();

    // Set the server to fail with a retryable status code for the max attempts
    int maxAttempts = RetryPolicy.getDefault().getMaxAttempts();
    int retryableCode = 503;
    for (int i = 0; i < maxAttempts; i++) {
      server.responses.add(HttpResponse.of(retryableCode));
    }

    // Result should be failure, sever should have received maxAttempts requests
    CompletableResultCode resultCode =
        exporter.apply(dataSupplier.get()).join(10, TimeUnit.SECONDS);
    assertThat(resultCode.isDone())
        .as("Exporter didn't complete in time. Consider increasing join timeout?")
        .isTrue();
    assertThat(resultCode.isSuccess()).isFalse();
    assertThat(serverRequestCountSupplier.get()).isEqualTo(maxAttempts);
  }
}
