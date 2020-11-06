/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.jupiter.api.Test;

class AccumulatorTest {
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("test", "1.0");

  @Test
  void collectionCycle() {
    Processor processor = mock(Processor.class);
    Accumulator accumulator = new Accumulator(processor);

    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testInstrument",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    accumulator.recordLongAdd(
        instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key1", "value1"), 24);
    accumulator.recordLongAdd(
        instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key2", "value2"), 12);

    verifyNoInteractions(processor);

    accumulator.collect();

    AggregatorKey expectedKey1 =
        AggregatorKey.create(
            instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key1", "value1"));
    AggregatorKey expectedKey2 =
        AggregatorKey.create(
            instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key2", "value2"));
    verify(processor).process(expectedKey1, LongAccumulation.create(24));
    verify(processor).process(expectedKey2, LongAccumulation.create(12));
  }

  @Test
  void collectionCycle_twoRecordingsSameKey() {
    Processor processor = mock(Processor.class);
    Accumulator accumulator = new Accumulator(processor);

    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testInstrument",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    Labels labels = Labels.of("key", "value");
    accumulator.recordLongAdd(instrumentationLibraryInfo, instrumentDescriptor, labels, 24);
    accumulator.recordLongAdd(instrumentationLibraryInfo, instrumentDescriptor, labels, 12);

    verifyNoInteractions(processor);

    accumulator.collect();

    AggregatorKey expectedKey =
        AggregatorKey.create(instrumentationLibraryInfo, instrumentDescriptor, labels);
    verify(processor).process(expectedKey, LongAccumulation.create(36));
  }
}
