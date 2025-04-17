/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class GrpcExporterTest {

  @Test
  void build_NoGrpcSenderProvider() {
    assertThatThrownBy(
            () ->
                new GrpcExporterBuilder<>(
                        "exporter",
                        ExporterMetrics.Signal.SPAN,
                        "testing",
                        10,
                        new URI("http://localhost"),
                        null,
                        "/path")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-upstream");
  }

  @ParameterizedTest
  @EnumSource
  @SuppressWarnings("unchecked")
  void testHealthMetrics(ExporterMetrics.Signal signal) {
    String signalMetricPrefix;
    String expectedUnit;
    switch (signal) {
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
      default:
        throw new IllegalStateException();
    }

    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build()) {

      ComponentId id = ComponentId.generateLazy("test_exporter");

      Attributes expectedAttributes =
          Attributes.builder()
              .put(SemConvAttributes.OTEL_COMPONENT_TYPE, "test_exporter")
              .put(SemConvAttributes.OTEL_COMPONENT_NAME, id.getComponentName())
              .put("foo", "bar")
              .build();

      GrpcSender<Marshaler> mockSender = Mockito.mock(GrpcSender.class);
      Marshaler mockMarshaller = Mockito.mock(Marshaler.class);

      GrpcExporter<Marshaler> exporter =
          new GrpcExporter<Marshaler>(
              "legacy_exporter",
              signal,
              mockSender,
              HealthMetricLevel.ON,
              id,
              () -> meterProvider,
              Attributes.builder().put("foo", "bar").build());

      doAnswer(
              invoc -> {
                Consumer<GrpcResponse> onResponse = invoc.getArgument(1);

                assertThat(inMemoryMetrics.collectAllMetrics())
                    .hasSize(1)
                    .anySatisfy(
                        metric ->
                            OpenTelemetryAssertions.assertThat(metric)
                                .hasName(signalMetricPrefix + "inflight")
                                .hasUnit(expectedUnit)
                                .hasLongSumSatisfying(
                                    ma ->
                                        ma.isNotMonotonic()
                                            .hasPointsSatisfying(
                                                pa ->
                                                    pa.hasAttributes(expectedAttributes)
                                                        .hasValue(42))));

                onResponse.accept(GrpcResponse.create(0, null));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());

      exporter.export(mockMarshaller, 42);

      doAnswer(
              invoc -> {
                Consumer<GrpcResponse> onResponse = invoc.getArgument(1);
                onResponse.accept(
                    GrpcResponse.create(GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE, null));
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
                  OpenTelemetryAssertions.assertThat(metric)
                      .hasName(signalMetricPrefix + "inflight")
                      .hasUnit(expectedUnit)
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributes(expectedAttributes).hasValue(0))))
          .anySatisfy(
              metric ->
                  OpenTelemetryAssertions.assertThat(metric)
                      .hasName(signalMetricPrefix + "exported")
                      .hasUnit(expectedUnit)
                      .hasLongSumSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributes(expectedAttributes).hasValue(42),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.ERROR_TYPE,
                                                      "" + GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE)
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
                  OpenTelemetryAssertions.assertThat(metric)
                      .hasName("otel.sdk.exporter.operation.duration")
                      .hasUnit("s")
                      .hasHistogramSatisfying(
                          ma ->
                              ma.hasPointsSatisfying(
                                  pa -> pa.hasAttributes(expectedAttributes).hasBucketCounts(1),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.ERROR_TYPE,
                                                      "" + GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE)
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
  @SuppressWarnings("unchecked")
  void testHealthMetricsDisabled() {
    InMemoryMetricReader inMemoryMetrics = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(inMemoryMetrics).build()) {

      ComponentId id = ComponentId.generateLazy("test_exporter");
      GrpcSender<Marshaler> mockSender = Mockito.mock(GrpcSender.class);
      Marshaler mockMarshaller = Mockito.mock(Marshaler.class);

      GrpcExporter<Marshaler> exporter =
          new GrpcExporter<Marshaler>(
              "legacy_exporter",
              ExporterMetrics.Signal.SPAN,
              mockSender,
              HealthMetricLevel.OFF,
              id,
              () -> meterProvider,
              Attributes.empty());

      doAnswer(
              invoc -> {
                Consumer<GrpcResponse> onResponse = invoc.getArgument(1);
                onResponse.accept(GrpcResponse.create(0, null));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());
      exporter.export(mockMarshaller, 42);

      doAnswer(
              invoc -> {
                Consumer<GrpcResponse> onResponse = invoc.getArgument(1);
                onResponse.accept(
                    GrpcResponse.create(GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE, null));
                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());
      exporter.export(mockMarshaller, 42);

      assertThat(inMemoryMetrics.collectAllMetrics()).isEmpty();
    }
  }
}
