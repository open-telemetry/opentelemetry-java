/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.state;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.instrument.DoubleMeasurement;
import io.opentelemetry.sdk.metrics.view.AttributesProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AsynchronousInstrumentStorageTest {

  @Test
  @SuppressWarnings("unchecked")
  public void asynchronousStorage_usesAttributesProcessor() {
    final Aggregator<Object> mockAggregator = Mockito.mock(Aggregator.class);
    AsynchronousInstrumentStorage storage =
        AsynchronousInstrumentStorage.create(
            (ObservableDoubleMeasurement measure) ->
                measure.observe(1.0, Attributes.of(stringKey("k"), "v")),
            mockAggregator,
            (attributes, context) -> Attributes.empty());
    storage.collectAndReset(0);

    Mockito.verify(mockAggregator)
        .batchRecord(DoubleMeasurement.create(1.0, Attributes.empty(), Context.root()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void asynchronousStorage_sendsMeasurementsToAggregatorAndCompletes() {
    final Aggregator<Object> mockAggregator = Mockito.mock(Aggregator.class);
    AsynchronousInstrumentStorage storage =
        AsynchronousInstrumentStorage.create(
            (ObservableDoubleMeasurement measure) ->
                measure.observe(1.0, Attributes.of(stringKey("k"), "v")),
            mockAggregator,
            AttributesProcessor.NOOP);
    storage.collectAndReset(0);

    // Verify aggregator received mesurement and completion timestmap.
    Mockito.verify(mockAggregator)
        .batchRecord(
            DoubleMeasurement.create(1.0, Attributes.of(stringKey("k"), "v"), Context.root()));
    Mockito.verify(mockAggregator).completeCollectionCycle(0);
  }
}
