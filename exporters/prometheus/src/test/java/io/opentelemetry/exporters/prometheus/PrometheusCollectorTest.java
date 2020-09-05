/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.prometheus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PrometheusCollectorTest {
  @Mock MetricProducer metricProducer;
  PrometheusCollector prometheusCollector;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    prometheusCollector =
        PrometheusCollector.newBuilder().setMetricProducer(metricProducer).buildAndRegister();
  }

  @Test
  void registerToDefault() throws IOException {
    when(metricProducer.collectAllMetrics()).thenReturn(generateTestData());
    StringWriter stringWriter = new StringWriter();
    TextFormat.write004(stringWriter, CollectorRegistry.defaultRegistry.metricFamilySamples());
    assertThat(stringWriter.toString())
        .isEqualTo(
            "# HELP grpc_name long_description\n"
                + "# TYPE grpc_name counter\n"
                + "grpc_name{kc=\"vc\",kp=\"vp\",} 5.0\n"
                + "# HELP http_name double_description\n"
                + "# TYPE http_name counter\n"
                + "http_name{kc=\"vc\",kp=\"vp\",} 3.5\n");
  }

  private static ImmutableList<MetricData> generateTestData() {
    return ImmutableList.of(
        MetricData.create(
            Descriptor.create(
                "grpc.name",
                "long_description",
                "1",
                Descriptor.Type.MONOTONIC_LONG,
                Labels.of("kc", "vc")),
            Resource.create(Attributes.of("kr", AttributeValue.stringAttributeValue("vr"))),
            InstrumentationLibraryInfo.create("grpc", "version"),
            Collections.singletonList(
                MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))),
        MetricData.create(
            Descriptor.create(
                "http.name",
                "double_description",
                "1",
                Descriptor.Type.MONOTONIC_DOUBLE,
                Labels.of("kc", "vc")),
            Resource.create(Attributes.of("kr", AttributeValue.stringAttributeValue("vr"))),
            InstrumentationLibraryInfo.create("http", "version"),
            Collections.singletonList(
                MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 3.5))));
  }
}
