/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessorFactory;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SynchronousMetricStorageTest {
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
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
    SynchronousMetricStorage<?> accumulator =
        new SynchronousMetricStorage<>(
            METRIC_DESCRIPTOR,
            aggregator,
            new InstrumentProcessor<>(aggregator, testClock.now()),
            spyLabelsProcessor);
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
    SynchronousMetricStorage<?> accumulator =
        new SynchronousMetricStorage<>(
            METRIC_DESCRIPTOR,
            aggregator,
            new InstrumentProcessor<>(aggregator, testClock.now()),
            spyLabelsProcessor);
    BoundStorageHandle handle = accumulator.bind(labels);
    handle.recordDouble(1, labels, Context.root());
    MetricData md = accumulator.collectAndReset(0, testClock.now());
    assertThat(md)
        .hasDoubleGauge()
        .points()
        .allSatisfy(
            p ->
                assertThat(p)
                    .attributes()
                    .hasSize(2)
                    .containsEntry("modifiedK", "modifiedV")
                    .containsEntry("K", "V"));
  }

  @Test
  void sameAggregator_ForSameAttributes() {
    SynchronousMetricStorage<?> accumulator =
        new SynchronousMetricStorage<>(
            METRIC_DESCRIPTOR,
            aggregator,
            new InstrumentProcessor<>(aggregator, testClock.now()),
            labelsProcessor);
    BoundStorageHandle handle = accumulator.bind(Attributes.builder().put("K", "V").build());
    BoundStorageHandle duplicateHandle =
        accumulator.bind(Attributes.builder().put("K", "V").build());
    try {
      assertThat(duplicateHandle).isSameAs(handle);
      accumulator.collectAndReset(0, testClock.now());
      BoundStorageHandle anotherDuplicateAggregatorHandle =
          accumulator.bind(Attributes.builder().put("K", "V").build());
      try {
        assertThat(anotherDuplicateAggregatorHandle).isSameAs(handle);
      } finally {
        anotherDuplicateAggregatorHandle.release();
      }
    } finally {
      duplicateHandle.release();
      handle.release();
    }

    // If we try to collect once all bound references are gone AND no recordings have occurred, we
    // should not see any labels (or metric).
    assertThat(accumulator.collectAndReset(0, testClock.now())).isNull();
  }
}
