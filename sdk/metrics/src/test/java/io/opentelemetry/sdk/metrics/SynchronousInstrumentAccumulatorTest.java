/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

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
