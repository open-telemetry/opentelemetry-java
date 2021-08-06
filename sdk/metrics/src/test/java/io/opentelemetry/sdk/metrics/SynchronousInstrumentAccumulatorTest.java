/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
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
import io.opentelemetry.sdk.testing.time.TestClock;
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
    accumulator.bind(Attributes.empty());
    Mockito.verify(spyLabelsProcessor).onLabelsBound(Context.current(), Attributes.empty());
  }

  @Test
  void labelsProcessor_applied() {
    final Attributes labels = Attributes.builder().put("K", "V").build();
    LabelsProcessor labelsProcessor =
        new LabelsProcessor() {
          @Override
          public Attributes onLabelsBound(Context ctx, Attributes lbls) {
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
            p -> assertThat(p.getAttributes()).hasSize(1).containsEntry("modifiedK", "modifiedV"));
  }

  @Test
  void sameAggregator_ForSameLabelSet() {
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator, new InstrumentProcessor<>(aggregator, testClock.now()), labelsProcessor);
    AggregatorHandle<?> aggregatorHandle =
        accumulator.bind(Attributes.builder().put("K", "V").build());
    AggregatorHandle<?> duplicateAggregatorHandle =
        accumulator.bind(Attributes.builder().put("K", "V").build());
    try {
      assertThat(duplicateAggregatorHandle).isSameAs(aggregatorHandle);
      accumulator.collectAll(testClock.now());
      AggregatorHandle<?> anotherDuplicateAggregatorHandle =
          accumulator.bind(Attributes.builder().put("K", "V").build());
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
