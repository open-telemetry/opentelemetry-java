/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeLog;
import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeMetric;
import static io.opentelemetry.sdk.autoconfigure.OtlpGrpcServerExtension.generateFakeSpan;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.grpc.Status;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressLogger(OkHttpGrpcExporter.class)
class OtlpGrpcRetryTest {

  private static final List<SpanData> SPAN_DATA = Lists.newArrayList(generateFakeSpan());
  private static final List<MetricData> METRIC_DATA = Lists.newArrayList(generateFakeMetric());
  private static final List<LogRecordData> LOG_RECORD_DATA = Lists.newArrayList(generateFakeLog());

  @RegisterExtension
  @Order(1)
  public static final SelfSignedCertificateExtension certificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static final OtlpGrpcServerExtension server = new OtlpGrpcServerExtension(certificate);

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void configureSpanExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.traces.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.traces.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    try (SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp",
            DefaultConfigProperties.createForTest(props),
            NamedSpiManager.createEmpty(),
            MeterProvider.noop())) {

      testRetryableStatusCodes(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
      testDefaultRetryPolicy(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
    }
  }

  @Test
  @SuppressLogger(OkHttpGrpcExporter.class)
  void configureMetricExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.metrics.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    try (MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props))) {

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
    props.put("otel.exporter.otlp.logs.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.logs.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    try (LogRecordExporter logRecordExporter =
        LogRecordExporterConfiguration.configureOtlpLogs(
            DefaultConfigProperties.createForTest(props), MeterProvider.noop())) {
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
    for (Status.Code code : Status.Code.values()) {
      server.reset();

      server.responseStatuses.add(Status.fromCode(code));
      server.responseStatuses.add(Status.OK);

      CompletableResultCode resultCode =
          exporter.apply(dataSupplier.get()).join(10, TimeUnit.SECONDS);
      assertThat(resultCode.isDone())
          .as("Exporter didn't complete in time. Consider increasing join timeout?")
          .isTrue();

      boolean retryable =
          RetryUtil.retryableGrpcStatusCodes().contains(String.valueOf(code.value()));
      boolean expectedResult = retryable || code == Status.Code.OK;
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
    int retryableCode =
        RetryUtil.retryableGrpcStatusCodes().stream().map(Integer::parseInt).findFirst().get();
    for (int i = 0; i < maxAttempts; i++) {
      server.responseStatuses.add(Status.fromCodeValue(retryableCode));
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
