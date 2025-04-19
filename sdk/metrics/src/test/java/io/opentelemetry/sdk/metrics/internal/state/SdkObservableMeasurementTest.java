/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

@SuppressWarnings("rawtypes")
class SdkObservableMeasurementTest {

  @RegisterExtension
  final LogCapturer logs =
      LogCapturer.create().captureForLogger(SdkObservableMeasurement.class.getName(), Level.DEBUG);

  private AsynchronousMetricStorage mockAsyncStorage1;
  private AsynchronousMetricStorage mockAsyncStorage2;
  private RegisteredReader registeredReader1;
  private SdkObservableMeasurement sdkObservableMeasurement;

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

    mockAsyncStorage1 = mock(AsynchronousMetricStorage.class);
    when(mockAsyncStorage1.getRegisteredReader()).thenReturn(registeredReader1);
    mockAsyncStorage2 = mock(AsynchronousMetricStorage.class);
    when(mockAsyncStorage2.getRegisteredReader()).thenReturn(registeredReader2);

    sdkObservableMeasurement =
        SdkObservableMeasurement.create(
            instrumentationScopeInfo,
            instrumentDescriptor,
            Arrays.asList(mockAsyncStorage1, mockAsyncStorage2));
  }

  void setupAndSetActiveReader(MemoryMode memoryMode) {
    setup(memoryMode);
    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);
  }

  @Test
  void setActiveReader_SetsEpochInformation() {
    setup(MemoryMode.IMMUTABLE_DATA);

    sdkObservableMeasurement.setActiveReader(registeredReader1, 0, 10);

    verify(mockAsyncStorage1).setEpochInformation(0, 10);
    verify(mockAsyncStorage2).getRegisteredReader();
    verifyNoMoreInteractions(mockAsyncStorage2);
  }

  @Test
  void recordLong_ImmutableData() {
    setupAndSetActiveReader(MemoryMode.IMMUTABLE_DATA);

    try {
      sdkObservableMeasurement.record(5);
      verify(mockAsyncStorage1).record(Attributes.empty(), 5);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  void recordDouble_ImmutableData() {
    setupAndSetActiveReader(MemoryMode.IMMUTABLE_DATA);

    try {
      sdkObservableMeasurement.record(4.3);
      verify(mockAsyncStorage1).record(Attributes.empty(), 4.3);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  void recordDouble_ReusableData() {
    setupAndSetActiveReader(MemoryMode.REUSABLE_DATA);

    try {
      sdkObservableMeasurement.record(4.3);
      verify(mockAsyncStorage1).record(Attributes.empty(), 4.3);

      sdkObservableMeasurement.record(5.3);
      verify(mockAsyncStorage1).record(Attributes.empty(), 5.3);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  void recordLong_ReusableData() {
    setupAndSetActiveReader(MemoryMode.REUSABLE_DATA);

    try {
      sdkObservableMeasurement.record(2);
      verify(mockAsyncStorage1).record(Attributes.empty(), 2);

      sdkObservableMeasurement.record(6);
      verify(mockAsyncStorage1).record(Attributes.empty(), 6);
    } finally {
      sdkObservableMeasurement.unsetActiveReader();
    }
  }

  @Test
  @SuppressLogger(SdkObservableMeasurement.class)
  void recordDouble_NaN() {
    setupAndSetActiveReader(MemoryMode.REUSABLE_DATA);

    sdkObservableMeasurement.record(Double.NaN);
    verify(mockAsyncStorage1, never()).record(any(), anyDouble());
    logs.assertContains(
        "Instrument testCounter has recorded measurement Not-a-Number (NaN) value with attributes {}. Dropping measurement.");
  }
}
