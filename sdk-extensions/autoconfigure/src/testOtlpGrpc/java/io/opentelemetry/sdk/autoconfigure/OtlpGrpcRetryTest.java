/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil.retryableStatusCodes;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.grpc.Status;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OtlpGrpcRetryTest {

  private static final List<SpanData> SPAN_DATA =
      Lists.newArrayList(
          TestSpanData.builder()
              .setHasEnded(true)
              .setName("name")
              .setStartEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
              .setEndEpochNanos(MILLISECONDS.toNanos(System.currentTimeMillis()))
              .setKind(SpanKind.SERVER)
              .setStatus(StatusData.error())
              .setTotalRecordedEvents(0)
              .setTotalRecordedLinks(0)
              .build());
  private static final List<MetricData> METRIC_DATA =
      Lists.newArrayList(
          MetricData.createLongSum(
              Resource.empty(),
              InstrumentationLibraryInfo.empty(),
              "metric_name",
              "metric_description",
              "ms",
              LongSumData.create(
                  false,
                  AggregationTemporality.CUMULATIVE,
                  Collections.singletonList(
                      LongPointData.create(
                          MILLISECONDS.toNanos(System.currentTimeMillis()),
                          MILLISECONDS.toNanos(System.currentTimeMillis()),
                          Attributes.of(stringKey("key"), "value"),
                          10)))));

  @RegisterExtension
  @Order(1)
  public static final SelfSignedCertificateExtension certificate =
      new SelfSignedCertificateExtension();

  @RegisterExtension
  @Order(2)
  public static final OtlpGrpcServerExtension server = new OtlpGrpcServerExtension(certificate);

  @Test
  void configureSpanExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.traces.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.traces.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    SpanExporter spanExporter =
        SpanExporterConfiguration.configureExporter(
            "otlp", DefaultConfigProperties.createForTest(props), Collections.emptyMap());

    testRetryableStatusCodes(() -> SPAN_DATA, spanExporter::export, server.traceRequests::size);
  }

  @Test
  void configureMetricExporterRetryPolicy() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:" + server.httpsPort());
    props.put(
        "otel.exporter.otlp.metrics.certificate", certificate.certificateFile().getAbsolutePath());
    props.put("otel.experimental.exporter.otlp.retry.enabled", "true");
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureOtlpMetrics(
            DefaultConfigProperties.createForTest(props), SdkMeterProvider.builder());

    testRetryableStatusCodes(
        () -> METRIC_DATA, metricExporter::export, server.metricRequests::size);
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
      boolean retryable = retryableStatusCodes().contains(code);
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
}
