/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.linecorp.armeria.client.WebClient;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrometheusHttpServerTest {
  PrometheusHttpServer prometheusServer;

  @Mock MetricProducer metricProducer;

  @BeforeEach
  void setUp() {
    // Apply the SDK metric producer registers with prometheus.
    prometheusServer =
        (PrometheusHttpServer)
            PrometheusHttpServer.builder()
                .setHost("localhost")
                .setPort(0)
                .build()
                .apply(metricProducer);
  }

  @AfterEach
  void tearDown() {
    prometheusServer.shutdown();
  }

  @Test
  void invalidConfig() {
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setPort(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("port must be positive");
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setHost(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("host");
    assertThatThrownBy(() -> PrometheusHttpServer.builder().setHost(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("host must not be empty");
  }

  @Test
  void fetchMetrics() {
    when(metricProducer.collectAllMetrics()).thenReturn(generateTestData());

    String response =
        WebClient.of("http://localhost:" + prometheusServer.getAddress().getPort())
            .get("/metrics")
            .aggregate()
            .join()
            .contentUtf8();
    assertThat(response)
        .isEqualTo(
            "# HELP grpc_name_total long_description\n"
                + "# TYPE grpc_name_total counter\n"
                + "grpc_name_total{kp=\"vp\",} 5.0\n"
                + "# HELP http_name_total double_description\n"
                + "# TYPE http_name_total counter\n"
                + "http_name_total{kp=\"vp\",} 3.5\n");
  }

  private static ImmutableList<MetricData> generateTestData() {
    return ImmutableList.of(
        MetricData.createLongSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationLibraryInfo.create("grpc", "version"),
            "grpc.name",
            "long_description",
            "1",
            LongSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    LongPointData.create(123, 456, Attributes.of(stringKey("kp"), "vp"), 5)))),
        MetricData.createDoubleSum(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationLibraryInfo.create("http", "version"),
            "http.name",
            "double_description",
            "1",
            DoubleSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    DoublePointData.create(123, 456, Attributes.of(stringKey("kp"), "vp"), 3.5)))));
  }
}
