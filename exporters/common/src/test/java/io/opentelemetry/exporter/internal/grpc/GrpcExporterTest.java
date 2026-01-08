/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static io.opentelemetry.exporter.grpc.GrpcStatusCode.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.grpc.GrpcResponse;
import io.opentelemetry.exporter.grpc.GrpcSender;
import io.opentelemetry.exporter.grpc.GrpcStatusCode;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.internal.StandardComponentId;
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
                new GrpcExporterBuilder(
                        StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
                        10,
                        new URI("http://localhost"),
                        "service/method")
                    .build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "No GrpcSenderProvider found on classpath. Please add dependency on "
                + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-managed-channel");
  }

  @ParameterizedTest
  @EnumSource
  @SuppressLogger(GrpcExporter.class)
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

      GrpcSender mockSender = Mockito.mock(GrpcSender.class);
      Marshaler mockMarshaller = Mockito.mock(Marshaler.class);

      GrpcExporter exporter =
          new GrpcExporter(
              mockSender,
              InternalTelemetryVersion.LATEST,
              id,
              () -> meterProvider,
              URI.create("http://testing:1234"));

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

                onResponse.accept(
                    ImmutableGrpcResponse.create(GrpcStatusCode.OK, null, new byte[0]));

                return null;
              })
          .when(mockSender)
          .send(any(), any(), any());

      exporter.export(mockMarshaller, 42);

      doAnswer(
              invoc -> {
                Consumer<GrpcResponse> onResponse = invoc.getArgument(1);
                onResponse.accept(ImmutableGrpcResponse.create(UNAVAILABLE, null, new byte[0]));

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
                                                      "" + UNAVAILABLE.getValue())
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
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(SemConvAttributes.RPC_GRPC_STATUS_CODE, 0)
                                                  .build())
                                          .hasBucketCounts(1),
                                  pa ->
                                      pa.hasAttributes(
                                              expectedAttributes.toBuilder()
                                                  .put(
                                                      SemConvAttributes.ERROR_TYPE,
                                                      "" + UNAVAILABLE.getValue())
                                                  .put(
                                                      SemConvAttributes.RPC_GRPC_STATUS_CODE,
                                                      UNAVAILABLE.getValue())
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
}
