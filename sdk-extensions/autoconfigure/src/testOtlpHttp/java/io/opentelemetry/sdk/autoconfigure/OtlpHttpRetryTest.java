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
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpHttpRetryTest {

  private static final List<SpanData> SPAN_DATA = Lists.newArrayList(generateFakeSpan());
  private static final List<MetricData> METRIC_DATA = Lists.newArrayList(generateFakeMetric());
  private static final List<LogData> LOG_DATA = Lists.newArrayList(generateFakeLog());

  @RegisterExtension
  public static final OtlpHttpServerExtension server = new OtlpHttpServerExtension();

  @Test
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
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            DefaultConfigProperties.createForTest(props),
            Collections.emptyMap(),
            MeterProvider.noop());

    testRetryableStatusCodes(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
    testDefaultRetryPolicy(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
  }

  @Test
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
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props));

    testRetryableStatusCodes(
        () -> METRIC_DATA, metricExporter::export, server.metricRequests::size);
    testDefaultRetryPolicy(() -> METRIC_DATA, metricExporter::export, server.metricRequests::size);
  }

  @Test
  void configureLogExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.logs.protocol", "http/protobuf");
    props.put(
        "otel.exporter.otlp.logs.endpoint", "https://localhost:" + server.httpsPort() + "/v1/logs");
    props.put(
        "otel.exporter.otlp.logs.certificate",
        server.selfSignedCertificate.certificate().getPath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    LogExporter logExporter =
        LogExporterConfiguration.configureOtlpLogs(
            DefaultConfigProperties.createForTest(props), MeterProvider.noop());

    testRetryableStatusCodes(() -> LOG_DATA, logExporter::export, server.logRequests::size);
    testDefaultRetryPolicy(() -> LOG_DATA, logExporter::export, server.logRequests::size);
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
    assertThat(resultCode.isSuccess()).isFalse();
    assertThat(serverRequestCountSupplier.get()).isEqualTo(maxAttempts);
  }
}
