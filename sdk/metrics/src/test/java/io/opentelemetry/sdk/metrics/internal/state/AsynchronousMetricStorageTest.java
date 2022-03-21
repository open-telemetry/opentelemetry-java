/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AsynchronousMetricStorageTest {
  private final TestClock testClock = TestClock.create();
  private MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState =
      MeterSharedState.create(InstrumentationScopeInfo.empty());
  private RegisteredView registeredView;
  private CollectionHandle handle;
  private Set<CollectionHandle> all;

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(AsynchronousMetricStorage.class);

  @Mock private MetricReader reader;

  @BeforeEach
  void setup() {
    registeredView =
        RegisteredView.create(
            InstrumentSelector.builder().setType(InstrumentType.OBSERVABLE_GAUGE).build(),
            View.builder().setAggregation(Aggregation.lastValue()).build(),
            SourceInfo.fromCurrentStack());
    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                registeredView.getInstrumentSelector(),
                registeredView.getView(),
                registeredView.getViewSourceInfo())
            .build();

    meterProviderSharedState =
        MeterProviderSharedState.create(
            testClock, Resource.empty(), viewRegistry, ExemplarFilter.sampleWithTraces());

    handle = CollectionHandle.createSupplier().get();
    all = CollectionHandle.mutableSet();
    all.add(handle);
  }

  @Test
  void collectAndReset_CallsMultipleCallbacks() {
    AsynchronousMetricStorage<?, ObservableLongMeasurement> metricStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            registeredView,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG));
    // Callbacks partially overlap for the metrics they record, should take first registered
    Consumer<ObservableLongMeasurement> callback1 =
        measurement -> measurement.record(1, Attributes.builder().put("key", "a").build());
    Consumer<ObservableLongMeasurement> callback2 =
        measurement -> {
          measurement.record(3, Attributes.builder().put("key", "a").build());
          measurement.record(3, Attributes.builder().put("key", "b").build());
        };
    metricStorage.addCallback(callback1);
    metricStorage.addCallback(callback2);

    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationScopeInfo(),
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

    // Remove callback2, verify only callback1 is called
    metricStorage.removeCallback(callback2);
    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationScopeInfo(),
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
                        }));
  }

  @Test
  void collectAndReset_IgnoresDuplicates() {
    AsynchronousMetricStorage<?, ObservableLongMeasurement> metricStorage =
        AsynchronousMetricStorage.createLongAsyncStorage(
            registeredView,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG));
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
                meterSharedState.getInstrumentationScopeInfo(),
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
            registeredView,
            InstrumentDescriptor.create(
                "my-instrument",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG));
    metricStorage.addCallback(
        unused -> {
          throw new RuntimeException("Error!");
        });
    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationScopeInfo(),
                0,
                testClock.nanoTime(),
                /* suppressSynchronousCollection= */ false))
        .isEqualTo(EmptyMetricData.getInstance());
    logs.assertContains("An exception occurred invoking callback for instrument my-instrument.");
  }
}
