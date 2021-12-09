/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
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
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Mock private MetricReader reader;

  @BeforeEach
  void setup() {
    spyAttributesProcessor = Mockito.spy(AttributesProcessor.noop());
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
    AsynchronousMetricStorage.doubleAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.DOUBLE),
            value -> value.record(1.0, Attributes.empty()))
        .collectAndReset(
            CollectionInfo.create(handle, all, reader),
            meterProviderSharedState.getResource(),
            meterSharedState.getInstrumentationLibraryInfo(),
            0,
            testClock.now(),
            false);
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void longAsynchronousAccumulator_AttributesProcessor_used() {
    AsynchronousMetricStorage.longAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            value -> value.record(1, Attributes.empty()))
        .collectAndReset(
            CollectionInfo.create(handle, all, reader),
            meterProviderSharedState.getResource(),
            meterSharedState.getInstrumentationLibraryInfo(),
            0,
            testClock.nanoTime(),
            false);
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void collectAndReset_IgnoresDuplicates() {
    MetricStorage metricStorage =
        AsynchronousMetricStorage.longAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
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
                false))
        .satisfies(
            metricData -> {
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
                      });
            });
  }

  @Test
  void collectAndReset_IgnoresInvalidAccumulations() {
    AtomicLong counter = new AtomicLong(5);
    MetricStorage metricStorage =
        AsynchronousMetricStorage.longAsynchronousAccumulator(
            View.builder().build(),
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_SUM,
                InstrumentValueType.LONG),
            measurement -> measurement.record(counter.getAndDecrement()));

    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationLibraryInfo(),
                0,
                testClock.nanoTime(),
                false))
        .satisfies(
            metricData ->
                assertThat(metricData.getLongSumData().getPoints())
                    .satisfiesExactly(dataPoint -> assertThat(dataPoint.getValue()).isEqualTo(5)));

    assertThat(
            metricStorage.collectAndReset(
                CollectionInfo.create(handle, all, reader),
                meterProviderSharedState.getResource(),
                meterSharedState.getInstrumentationLibraryInfo(),
                0,
                testClock.nanoTime(),
                false))
        .satisfies(metricData -> assertThat(metricData.isEmpty()).isTrue());
  }

  @Test
  void collectAndReset_CallbackException() {
    MetricStorage metricStorage =
        AsynchronousMetricStorage.longAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
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
                false))
        .isEqualTo(EmptyMetricData.getInstance());
  }
}
