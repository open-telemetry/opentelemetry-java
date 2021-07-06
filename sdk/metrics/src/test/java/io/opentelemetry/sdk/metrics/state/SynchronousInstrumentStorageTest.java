/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.ExemplarSampler;
import io.opentelemetry.sdk.metrics.aggregator.SynchronousHandle;
import io.opentelemetry.sdk.metrics.instrument.LongMeasurement;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import io.opentelemetry.sdk.metrics.view.AttributesProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SynchronousInstrumentStorageTest {
  @Test
  @SuppressWarnings("unchecked")
  public void synchronousStorage_sendsCompleteCollectionCycleToAggregator() {
    final Aggregator<Object> mockAggregator = Mockito.mock(Aggregator.class);
    SynchronousInstrumentStorage<Object> storage =
        SynchronousInstrumentStorage.create(mockAggregator, AttributesProcessor.NOOP);
    storage.collectAndReset(null, null, 0);

    // Verify aggregator received mesurement and completion timestmap.
    Mockito.verify(mockAggregator).completeCollectionCycle(0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void synchronousStorage_pullsHandlesFromAggregatorPerAttribute() {
    final Aggregator<Object> mockAggregator = Mockito.mock(Aggregator.class);
    final Attributes KV = Attributes.of(AttributeKey.stringKey("k"), "v");
    Mockito.when(mockAggregator.createStreamStorage()).thenAnswer(invocation -> new NoopHandle());
    SynchronousInstrumentStorage<Object> storage =
        SynchronousInstrumentStorage.create(mockAggregator, AttributesProcessor.NOOP);
    // Now we make sure we create N storage handles, and ensure the attributes
    // show up when we aggregate via `batchStreamAccumulate`.
    storage
        .bind(Attributes.empty())
        .record(LongMeasurement.create(1, Attributes.empty(), Context.root()));
    storage.bind(KV).record(LongMeasurement.create(1, KV, Context.root()));
    // Binding a handle with no value will NOT cause accumulation.
    storage.bind(Attributes.of(AttributeKey.stringKey("k"), "unused"));
    storage.collectAndReset(null, null, 0);
    Mockito.verify(mockAggregator).batchStreamAccumulation(Attributes.empty(), "result");
    Mockito.verify(mockAggregator).batchStreamAccumulation(KV, "result");
    Mockito.verify(mockAggregator).completeCollectionCycle(0);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void synchronousStorage_usesAttributeProcessor() {
    final Aggregator<Object> mockAggregator = Mockito.mock(Aggregator.class);
    final Attributes KV = Attributes.of(AttributeKey.stringKey("k"), "v");
    final Attributes KV2 = Attributes.of(AttributeKey.stringKey("k"), "v2");
    Mockito.when(mockAggregator.createStreamStorage()).thenAnswer(invocation -> new NoopHandle());
    SynchronousInstrumentStorage<Object> storage =
        SynchronousInstrumentStorage.create(
            mockAggregator,
            (attributes, context) -> {
              if (attributes.equals(KV)) {
                return KV2;
              }
              return attributes;
            });
    // Now we make sure we create N storage handles, and ensure the attributes
    // show up when we aggregate via `batchStreamAccumulate`.
    storage
        .bind(Attributes.empty())
        .record(LongMeasurement.create(1, Attributes.empty(), Context.root()));
    storage.bind(KV).record(LongMeasurement.create(1, KV, Context.root()));
    // Binding a handle with no value will NOT cause accumulation.
    storage.bind(Attributes.of(AttributeKey.stringKey("k"), "unused"));
    storage.collectAndReset(null, null, 0);
    Mockito.verify(mockAggregator).batchStreamAccumulation(Attributes.empty(), "result");
    Mockito.verify(mockAggregator).batchStreamAccumulation(KV2, "result");
    Mockito.verify(mockAggregator).completeCollectionCycle(0);
  }

  /** Stubbed version of synchronous handle for testing. */
  private static class NoopHandle extends SynchronousHandle<Object> {
    NoopHandle() {
      super(ExemplarSampler.NEVER);
    }

    @Override
    protected Object doAccumulateThenReset(Iterable<Measurement> exemplars) {
      return "result";
    }

    @Override
    protected void doRecord(Measurement value) {}
  }
}
