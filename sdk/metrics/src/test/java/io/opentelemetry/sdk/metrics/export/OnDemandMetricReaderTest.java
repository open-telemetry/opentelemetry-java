/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnDemandMetricReaderTest {

  private MetricExporter metricExporter;
  private CollectionRegistration collectionRegistration;
  private OnDemandMetricReader onDemandMetricReader;

  @BeforeEach
  void setup() {
    metricExporter = mock(MetricExporter.class);
    collectionRegistration = mock(CollectionRegistration.class);
    onDemandMetricReader = new OnDemandMetricReader(metricExporter);
    onDemandMetricReader.register(collectionRegistration);
  }

  @Test
  void forceFlush_ExportsMetrics() {
    List<MetricData> metrics = Collections.singletonList(mock(MetricData.class));
    when(collectionRegistration.collectAllMetrics()).thenReturn(metrics);
    when(metricExporter.export(metrics)).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode result = onDemandMetricReader.forceFlush();

    assertThat(result.isSuccess()).isTrue();
    verify(collectionRegistration).collectAllMetrics();
    verify(metricExporter).export(metrics);
  }

  @Test
  void forceFlush_NoMetrics() {
    when(collectionRegistration.collectAllMetrics()).thenReturn(Collections.emptyList());

    CompletableResultCode result = onDemandMetricReader.forceFlush();

    assertThat(result.isSuccess()).isTrue();
    verify(collectionRegistration).collectAllMetrics();
  }

  @Test
  void forceFlush_ExporterFails() {
    List<MetricData> metrics = Collections.singletonList(mock(MetricData.class));
    when(collectionRegistration.collectAllMetrics()).thenReturn(metrics);
    when(metricExporter.export(metrics)).thenReturn(CompletableResultCode.ofFailure());

    CompletableResultCode result = onDemandMetricReader.forceFlush();

    assertThat(result.isSuccess()).isFalse();
    verify(collectionRegistration).collectAllMetrics();
    verify(metricExporter).export(metrics);
  }

  @Test
  void shutdown_DelegatesToExporter() {
    when(metricExporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());

    CompletableResultCode result = onDemandMetricReader.shutdown();

    assertThat(result.isSuccess()).isTrue();
    verify(metricExporter).shutdown();
  }

  @Test
  void toString_ReturnsExpectedString() {
    when(metricExporter.toString()).thenReturn("MockMetricExporter{}");

    assertThat(onDemandMetricReader.toString())
        .isEqualTo("OnDemandMetricReader{exporter=MockMetricExporter{}}");
  }
}
