/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsynchronousMetricStorageTest {
  private final TestClock testClock = TestClock.create();
  private MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState =
      MeterSharedState.create(InstrumentationLibraryInfo.empty());
  private AttributesProcessor spyAttributesProcessor;
  private View view;
  private CollectionHandle handle;
  private Set<CollectionHandle> all;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(AsynchronousMetricStorage.class);

  @Mock private MetricReader reader;

  @BeforeEach
  void setup() {
    spyAttributesProcessor = spy(AttributesProcessor.noop());
    view =
        View.builder()
            .setAggregation(Aggregation.lastValue())
            .setAttributesProcessor(spyAttributesProcessor)
            .build();
    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
                    .build(),
                view)
            .build();

    meterProviderSharedState =
        MeterProviderSharedState.create(
            testClock, Resource.empty(), viewRegistry, ExemplarFilter.sampleWithTraces());

    handle = CollectionHandle.createSupplier().get();
    all = CollectionHandle.mutableSet();
    all.add(handle);
  }

  @Test
  void doubleAsynchronousAccumulator_AttributesProcessor_used() {
    AsynchronousMetricStorage<?, ObservableDoubleMeasurement> doubleAsyncStorage =
        AsynchronousMetricStorage.createDoubleAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.DOUBLE),
            new MetricStorageRegistry());
    doubleAsyncStorage.addCallback(value -> value.record(1.0, Attributes.empty()));
    doubleAsyncStorage.collectAndReset(
        CollectionInfo.create(handle, all, reader),
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        0,
        testClock.now(),
        /* suppressSynchronousCollection= */ false);
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void longAsynchronousAccumulator_AttributesProcessor_used() {
    AsynchronousMetricStorage<?, ObservableLongMeasurement> longAsyncStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            new MetricStorageRegistry());
    longAsyncStorage.addCallback(value -> value.record(1, Attributes.empty()));
    longAsyncStorage.collectAndReset(
        CollectionInfo.create(handle, all, reader),
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        0,
        testClock.nanoTime(),
        /* suppressSynchronousCollection= */ false);
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void registerAndUnregister() {
    MetricStorageRegistry storageRegistry = spy(new MetricStorageRegistry());
    AsynchronousMetricStorage<?, ObservableLongMeasurement> metricStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            storageRegistry);

    Consumer<ObservableLongMeasurement> callback1 =
        measurement -> measurement.record(10, Attributes.builder().put("key", "a").build());
    Consumer<ObservableLongMeasurement> callback2 =
        measurement -> measurement.record(10, Attributes.builder().put("key", "b").build());

    // Add both callbacks, and remove the first
    metricStorage.addCallback(callback1);
    metricStorage.addCallback(callback2);
    metricStorage.removeCallback(callback1);

    logs.assertDoesNotContain(
        "Attempting to add callback for my-instrument after storage has been unregistered.");
    logs.assertDoesNotContain(
        "Attempting to remove callback for my-instrument after storage has been unregistered.");
    verify(storageRegistry, never()).unregister(any());

    // Remove second callback
    metricStorage.removeCallback(callback2);

    logs.assertDoesNotContain(
        "Attempting to add callback for my-instrument after storage has been unregistered.");
    logs.assertDoesNotContain(
        "Attempting to remove callback for my-instrument after storage has been unregistered.");
    verify(storageRegistry).unregister(metricStorage);

    // Attempting to add or remove after all callbacks are removed logs warning
    Mockito.reset(storageRegistry);
    metricStorage.addCallback(callback1);
    logs.assertContains(
        "Attempting to add callback for my-instrument after storage has been unregistered.");
    metricStorage.removeCallback(callback1);
    logs.assertContains(
        "Attempting to remove callback for my-instrument after storage has been unregistered.");
    verify(storageRegistry, never()).unregister(any());
  }

  @Test
  void collectAndReset_CallsMultipleCallbacks() {
    AsynchronousMetricStorage<?, ObservableLongMeasurement> metricStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            new MetricStorageRegistry());
    // Callbacks partially overlap for the metrics they record, should take first registered
    metricStorage.addCallback(
        measurement -> measurement.record(1, Attributes.builder().put("key", "a").build()));
    metricStorage.addCallback(
        measurement -> {
          measurement.record(2, Attributes.builder().put("key", "a").build());
          measurement.record(3, Attributes.builder().put("key", "b").build());
        });

    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationLibraryInfo(),
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongGaugeData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        dataPoint -> {
                          assertThat(dataPoint.getValue()).isEqualTo(1);
                          assertThat(dataPoint.getAttributes())
                              .isEqualTo(Attributes.builder().put("key", "a").build());
                        },
                        dataPoint -> {
                          assertThat(dataPoint.getValue()).isEqualTo(3);
                          assertThat(dataPoint.getAttributes())
                              .isEqualTo(Attributes.builder().put("key", "b").build());
                        }));
    logs.assertContains(
        "Instrument my-instrument has recorded multiple values for the same attributes.");
  }

  @Test
  void collectAndReset_IgnoresDuplicates() {
    AsynchronousMetricStorage<?, ObservableLongMeasurement> metricStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            new MetricStorageRegistry());
    metricStorage.addCallback(
        measurement -> {
          measurement.record(1, Attributes.builder().put("key", "a").build());
          measurement.record(2, Attributes.builder().put("key", "a").build());
          measurement.record(3, Attributes.builder().put("key", "b").build());
        });
    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationLibraryInfo(),
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongGaugeData().getPoints())
                    .satisfiesExactlyInAnyOrder(
                        dataPoint -> {
                          assertThat(dataPoint.getValue()).isEqualTo(1);
                          assertThat(dataPoint.getAttributes())
                              .isEqualTo(Attributes.builder().put("key", "a").build());
                        },
                        dataPoint -> {
                          assertThat(dataPoint.getValue()).isEqualTo(3);
                          assertThat(dataPoint.getAttributes())
                              .isEqualTo(Attributes.builder().put("key", "b").build());
                        }));
    logs.assertContains(
        "Instrument my-instrument has recorded multiple values for the same attributes.");
  }

  @Test
  @SuppressLogger(AsynchronousMetricStorage.class)
  void collectAndReset_CallbackException() {
    AsynchronousMetricStorage<?, ObservableDoubleMeasurement> metricStorage =
        AsynchronousMetricStorage.createDoubleAsyncStorage(
            view,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            new MetricStorageRegistry());
    metricStorage.addCallback(
        unused -> {
          throw new RuntimeException("Error!");
        });
    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationLibraryInfo(),
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .isEqualTo(EmptyMetricData.getInstance());
    logs.assertContains("An exception occurred invoking callback for instrument my-instrument.");
  }
}
