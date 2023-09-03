/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@SuppressWarnings("rawtypes")
public class SdkObservableMeasurementTest {

  private AsynchronousMetricStorage mockAsyncStorage1;
  private RegisteredReader registeredReader1;
  private SdkObservableMeasurement sdkObservableMeasurement;
  private ArgumentCaptor<Measurement> measurementArgumentCaptor;

  @SuppressWarnings("unchecked")
  private void setup(MemoryMode memoryMode) {
    InstrumentationScopeInfo instrumentationScopeInfo =
        InstrumentationScopeInfo.builder("test-scope").build();
    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "an instrument for testing purposes",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG,
            Advice.empty());

    InMemoryMetricReader reader1 =
        InMemoryMetricReader.builder()
            .setAggregationTemporalitySelector(instrumentType -> CUMULATIVE)
            .setMemoryMode(memoryMode)
            .build();
    registeredReader1 = RegisteredReader.create(reader1, ViewRegistry.create());

    InMemoryMetricReader reader2 = InMemoryMetricReader.builder().setMemoryMode(memoryMode).build();
    RegisteredReader registeredReader2 = RegisteredReader.create(reader2, ViewRegistry.create());

    measurementArgumentCaptor = ArgumentCaptor.forClass(Measurement.class);
    mockAsyncStorage1 = mock(AsynchronousMetricStorage.class);
    when(mockAsyncStorage1.getRegisteredReader()).thenReturn(registeredReader1);
    AsynchronousMetricStorage mockAsyncStorage2 = mock(AsynchronousMetricStorage.class);
    when(mockAsyncStorage2.getRegisteredReader()).thenReturn(registeredReader2);

    sdkObservableMeasurement =
        SdkObservableMeasurement.create(
            instrumentationScopeInfo,
            instrumentDescriptor,
            Lists.newArrayList(mockAsyncStorage1, mockAsyncStorage2));
  }

  @Test
  public void testRecordLongImmutableData() {
    setup(MemoryMode.IMMUTABLE_DATA);

    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);

    try {
      sdkObservableMeasurement.record(5);

      verify(mockAsyncStorage1).record(measurementArgumentCaptor.capture());
      Measurement passedMeasurement = measurementArgumentCaptor.getValue();
      assertThat(passedMeasurement).isInstanceOf(ImmutableMeasurement.class);
      assertThat(passedMeasurement.longValue()).isEqualTo(5);
      assertThat(passedMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(passedMeasurement.epochNanos()).isEqualTo(10);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  public void testRecordDoubleReturnImmutableData() {
    setup(MemoryMode.IMMUTABLE_DATA);

    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);

    try {
      sdkObservableMeasurement.record(4.3);

      verify(mockAsyncStorage1).record(measurementArgumentCaptor.capture());
      Measurement passedMeasurement = measurementArgumentCaptor.getValue();
      assertThat(passedMeasurement).isInstanceOf(ImmutableMeasurement.class);
      assertThat(passedMeasurement.doubleValue()).isEqualTo(4.3);
      assertThat(passedMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(passedMeasurement.epochNanos()).isEqualTo(10);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  public void testRecordDoubleReturnReusableData() {
    setup(MemoryMode.REUSABLE_DATA);

    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);

    try {
      sdkObservableMeasurement.record(4.3);

      verify(mockAsyncStorage1).record(measurementArgumentCaptor.capture());
      Measurement firstMeasurement = measurementArgumentCaptor.getValue();
      assertThat(firstMeasurement).isInstanceOf(MutableMeasurement.class);
      assertThat(firstMeasurement.doubleValue()).isEqualTo(4.3);
      assertThat(firstMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(firstMeasurement.epochNanos()).isEqualTo(10);

      sdkObservableMeasurement.record(5.3);

      verify(mockAsyncStorage1, times(2)).record(measurementArgumentCaptor.capture());
      Measurement secondMeasurement = measurementArgumentCaptor.getValue();
      assertThat(secondMeasurement).isInstanceOf(MutableMeasurement.class);
      assertThat(secondMeasurement.doubleValue()).isEqualTo(5.3);
      assertThat(secondMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(secondMeasurement.epochNanos()).isEqualTo(10);

      // LeasedMeasurement should be re-used
      assertThat(secondMeasurement).isSameAs(firstMeasurement);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  public void testRecordLongReturnReusableData() {
    setup(MemoryMode.REUSABLE_DATA);

    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);

    try {
      sdkObservableMeasurement.record(2);

      verify(mockAsyncStorage1).record(measurementArgumentCaptor.capture());
      Measurement firstMeasurement = measurementArgumentCaptor.getValue();
      assertThat(firstMeasurement).isInstanceOf(MutableMeasurement.class);
      assertThat(firstMeasurement.longValue()).isEqualTo(2);
      assertThat(firstMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(firstMeasurement.epochNanos()).isEqualTo(10);

      sdkObservableMeasurement.record(6);

      verify(mockAsyncStorage1, times(2)).record(measurementArgumentCaptor.capture());
      Measurement secondMeasurement = measurementArgumentCaptor.getValue();
      assertThat(secondMeasurement).isInstanceOf(MutableMeasurement.class);
      assertThat(secondMeasurement.longValue()).isEqualTo(6);
      assertThat(secondMeasurement.startEpochNanos()).isEqualTo(0);
      assertThat(secondMeasurement.epochNanos()).isEqualTo(10);

      // LeasedMeasurement should be re-used
      assertThat(secondMeasurement).isSameAs(firstMeasurement);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }
}
