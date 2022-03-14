/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.export.CollectionInfo;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SynchronousMetricStorageTest {
  private static final Resource RESOURCE = Resource.empty();
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("test", "1.0", null);
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create("name", "description", "unit");
  private final TestClock testClock = TestClock.create();
  private final Aggregator<Long> aggregator =
      ((AggregatorFactory) Aggregation.lastValue())
          .createAggregator(DESCRIPTOR, ExemplarFilter.neverSample());
  private final AttributesProcessor attributesProcessor = AttributesProcessor.noop();
  private CollectionHandle collector;
  private Set<CollectionHandle> allCollectors;

  @Mock private MetricReader reader;

  @BeforeEach
  void setup() {
    collector = CollectionHandle.createSupplier().get();
    allCollectors = CollectionHandle.mutableSet();
    allCollectors.add(collector);
  }

  @Test
  void attributesProcessor_used() {
    AttributesProcessor spyAttributesProcessor = Mockito.spy(this.attributesProcessor);
    SynchronousMetricStorage accumulator =
        new DefaultSynchronousMetricStorage<>(
            METRIC_DESCRIPTOR, aggregator, spyAttributesProcessor);
    accumulator.bind(Attributes.empty());
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void attributesProcessor_applied() {
    Attributes labels = Attributes.builder().put("K", "V").build();
    AttributesProcessor attributesProcessor =
        AttributesProcessor.append(Attributes.builder().put("modifiedK", "modifiedV").build());
    AttributesProcessor spyLabelsProcessor = Mockito.spy(attributesProcessor);
    SynchronousMetricStorage accumulator =
        new DefaultSynchronousMetricStorage<>(METRIC_DESCRIPTOR, aggregator, spyLabelsProcessor);
    BoundStorageHandle handle = accumulator.bind(labels);
    handle.recordDouble(1, labels, Context.root());
    MetricData md =
        accumulator.collectAndReset(
            CollectionInfo.create(collector, allCollectors, reader),
            RESOURCE,
            INSTRUMENTATION_SCOPE_INFO,
            0,
            testClock.now(),
            false);
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
    SynchronousMetricStorage accumulator =
        new DefaultSynchronousMetricStorage<>(METRIC_DESCRIPTOR, aggregator, attributesProcessor);
    BoundStorageHandle handle = accumulator.bind(Attributes.builder().put("K", "V").build());
    BoundStorageHandle duplicateHandle =
        accumulator.bind(Attributes.builder().put("K", "V").build());
    try {
      assertThat(duplicateHandle).isSameAs(handle);
      accumulator.collectAndReset(
          CollectionInfo.create(collector, allCollectors, reader),
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          0,
          testClock.now(),
          false);
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
    assertThat(
            accumulator.collectAndReset(
                CollectionInfo.create(collector, allCollectors, reader),
                RESOURCE,
                INSTRUMENTATION_SCOPE_INFO,
                0,
                testClock.now(),
                false))
        .isEqualTo(EmptyMetricData.getInstance());
  }
}
