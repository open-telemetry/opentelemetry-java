/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
// Tests deprecated class
@SuppressWarnings("deprecation")
class PrometheusCollectorTest {
  @Mock MetricProducer metricProducer;
  PrometheusCollector prometheusCollector;

  @BeforeEach
  void setUp() {
    // Apply the SDK metric producer registers with prometheus.
    prometheusCollector = new PrometheusCollector(metricProducer);
    prometheusCollector.register();
  }

  @Test
  void registerWithSdkMeterProvider() {
    assertThatCode(
            () ->
                SdkMeterProvider.builder()
                    .registerMetricReader(PrometheusCollector.create())
                    .build()
                    .forceFlush()
                    .join(10, TimeUnit.SECONDS))
        .doesNotThrowAnyException();
  }

  @Test
  void registerToDefault() throws IOException {
    when(metricProducer.collectAllMetrics()).thenReturn(generateTestData());
    StringWriter stringWriter = new StringWriter();
    TextFormat.write004(stringWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
    assertThat(stringWriter.toString())
        .isEqualTo(
            "# HELP grpc_name_total long_description\n"
                + "# TYPE grpc_name_total counter\n"
                + "grpc_name_total{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP http_name_total double_description\n"
                + "# TYPE http_name_total counter\n"
                + "http_name_total{kp=\"vp\",} 3.5 1633950672000\n");
  }

  private static ImmutableList<MetricData> generateTestData() {
    return ImmutableList.of(
        ImmutableMetricData.createLongSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationScopeInfo.create("grpc", "version", null),
            "grpc.name",
            "long_description",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableLongPointData.create(
                        1633947011000000000L,
                        1633950672000000000L,
                        Attributes.of(stringKey("kp"), "vp"),
                        5)))),
        ImmutableMetricData.createDoubleSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationScopeInfo.create("http", "version", null),
            "http.name",
            "double_description",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableDoublePointData.create(
                        1633947011000000000L,
                        1633950672000000000L,
                        Attributes.of(stringKey("kp"), "vp"),
                        3.5)))));
  }
}
