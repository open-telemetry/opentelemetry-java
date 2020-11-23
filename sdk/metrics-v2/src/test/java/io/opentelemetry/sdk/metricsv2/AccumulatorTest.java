/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AccumulatorTest {
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("test", "1.0");

  @Test
  void collectionCycle() {
    Processor processor = mock(Processor.class);
    TestClock clock = TestClock.create();
    Accumulator accumulator = new Accumulator(clock);

    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testInstrument",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    InstrumentKey instrumentKey =
        InstrumentKey.create(instrumentDescriptor, instrumentationLibraryInfo);
    accumulator.recordLongAdd(instrumentKey, Labels.of("key1", "value1"), 24);
    accumulator.recordLongAdd(instrumentKey, Labels.of("key2", "value2"), 12);

    verifyNoInteractions(processor);

    accumulator.collectAndSendTo(processor);

    AggregatorKey expectedKey1 =
        AggregatorKey.create(
            instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key1", "value1"));
    AggregatorKey expectedKey2 =
        AggregatorKey.create(
            instrumentationLibraryInfo, instrumentDescriptor, Labels.of("key2", "value2"));
    verify(processor)
        .process(instrumentKey, expectedKey1, LongAccumulation.create(clock.now(), 24));
    verify(processor)
        .process(instrumentKey, expectedKey2, LongAccumulation.create(clock.now(), 12));
  }

  @Test
  void collectionCycle_twoRecordingsSameKey() {
    Processor processor = mock(Processor.class);
    TestClock clock = TestClock.create();
    Accumulator accumulator = new Accumulator(clock);

    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testInstrument",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    InstrumentKey instrumentKey =
        InstrumentKey.create(instrumentDescriptor, instrumentationLibraryInfo);

    Labels labels = Labels.of("key", "value");

    accumulator.recordLongAdd(instrumentKey, labels, 24);
    accumulator.recordLongAdd(instrumentKey, labels, 12);

    verifyNoInteractions(processor);

    accumulator.collectAndSendTo(processor);

    AggregatorKey expectedKey =
        AggregatorKey.create(instrumentationLibraryInfo, instrumentDescriptor, labels);
    verify(processor).process(instrumentKey, expectedKey, LongAccumulation.create(clock.now(), 36));
  }

  @Test
  void collectionCycle_twoRecordingsCycles() {
    Processor processor = mock(Processor.class);
    TestClock clock = TestClock.create();
    Accumulator accumulator = new Accumulator(clock);

    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            "testInstrument",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    InstrumentKey instrumentKey =
        InstrumentKey.create(instrumentDescriptor, instrumentationLibraryInfo);

    Labels labels = Labels.of("key", "value");
    accumulator.recordLongAdd(instrumentKey, labels, 24);

    verifyNoInteractions(processor);

    accumulator.collectAndSendTo(processor);

    AggregatorKey expectedKey =
        AggregatorKey.create(instrumentationLibraryInfo, instrumentDescriptor, labels);

    verify(processor, only())
        .process(instrumentKey, expectedKey, LongAccumulation.create(clock.now(), 24));

    // reset here so we don't get confused by the first cycle
    Mockito.reset(processor);
    accumulator.recordLongAdd(instrumentKey, labels, 12);
    accumulator.collectAndSendTo(processor);

    verify(processor, only())
        .process(instrumentKey, expectedKey, LongAccumulation.create(clock.now(), 12));
  }
}
