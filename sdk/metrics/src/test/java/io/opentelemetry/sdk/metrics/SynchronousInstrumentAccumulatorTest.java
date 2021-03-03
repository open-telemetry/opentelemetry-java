/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SynchronousInstrumentAccumulatorTest {
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private final TestClock testClock = TestClock.create();
  private final Aggregator<Long> aggregator =
      AggregatorFactory.lastValue()
          .create(Resource.empty(), InstrumentationLibraryInfo.create("test", "1.0"), DESCRIPTOR);
  private final LabelsProcessor labelsProcessor =
      LabelsProcessorFactory.noop()
          .create(Resource.empty(), InstrumentationLibraryInfo.create("test", "1.0"), DESCRIPTOR);

  @Test
  void labelsProcessor_used() {
    LabelsProcessor spyLabelsProcessor = Mockito.spy(this.labelsProcessor);
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator, new InstrumentProcessor<>(aggregator, testClock.now()), spyLabelsProcessor);
    accumulator.bind(Labels.empty());
    Mockito.verify(spyLabelsProcessor).onLabelsBound(Context.current(), Labels.empty());
  }

  @Test
  void labelsProcessor_applied() {
    final Labels labels = Labels.of("K", "V");
    LabelsProcessor labelsProcessor =
        new LabelsProcessor() {
          @Override
          public Labels onLabelsBound(Context ctx, Labels lbls) {
            return lbls.toBuilder().put("modifiedK", "modifiedV").build();
          }
        };
    LabelsProcessor spyLabelsProcessor = Mockito.spy(labelsProcessor);
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator, new InstrumentProcessor<>(aggregator, testClock.now()), spyLabelsProcessor);
    AggregatorHandle<?> aggregatorHandle = accumulator.bind(labels);
    aggregatorHandle.recordDouble(1);
    List<MetricData> md = accumulator.collectAll(testClock.now());
    md.stream()
        .flatMap(m -> m.getLongGaugeData().getPoints().stream())
        .forEach(
            p ->
                assertThat(new HashMap<>(p.getLabels().asMap()))
                    .isEqualTo(
                        new HashMap<>(
                            labels.toBuilder().put("modifiedK", "modifiedV").build().asMap())));
  }

  @Test
  void sameAggregator_ForSameLabelSet() {
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator, new InstrumentProcessor<>(aggregator, testClock.now()), labelsProcessor);
    AggregatorHandle<?> aggregatorHandle = accumulator.bind(Labels.of("K", "V"));
    AggregatorHandle<?> duplicateAggregatorHandle = accumulator.bind(Labels.of("K", "V"));
    try {
      assertThat(duplicateAggregatorHandle).isSameAs(aggregatorHandle);
      accumulator.collectAll(testClock.now());
      AggregatorHandle<?> anotherDuplicateAggregatorHandle = accumulator.bind(Labels.of("K", "V"));
      try {
        assertThat(anotherDuplicateAggregatorHandle).isSameAs(aggregatorHandle);
      } finally {
        anotherDuplicateAggregatorHandle.release();
      }
    } finally {
      duplicateAggregatorHandle.release();
      aggregatorHandle.release();
    }

    // At this point we should be able to unmap because all references are gone. Because this is an
    // internal detail we cannot call collectAll after this anymore.
    assertThat(aggregatorHandle.tryUnmap()).isTrue();
  }
}
