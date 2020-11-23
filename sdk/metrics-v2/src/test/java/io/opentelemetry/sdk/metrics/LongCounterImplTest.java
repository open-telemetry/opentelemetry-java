/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LongCounterImplTest {
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("test", "1.0");
  @Mock private Accumulator accumulator;

  @Test
  void record_passthrough_noLabels() {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);
    InstrumentKey instrumentKey = InstrumentKey.create(descriptor, instrumentationLibraryInfo);

    LongCounterImpl longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");

    longCounter.add(100);

    verify(accumulator).recordLongAdd(instrumentKey, Labels.empty(), 100);
  }

  @Test
  void record_passthrough_labels() {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);
    InstrumentKey instrumentKey = InstrumentKey.create(descriptor, instrumentationLibraryInfo);

    LongCounterImpl longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");

    longCounter.add(100, Labels.of("key", "value"));

    verify(accumulator).recordLongAdd(instrumentKey, Labels.of("key", "value"), 100);
  }

  @Test
  void record_passthrough_boundLabels() {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);
    InstrumentKey instrumentKey = InstrumentKey.create(descriptor, instrumentationLibraryInfo);

    LongCounter longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");
    LongCounter.BoundLongCounter boundLongCounter = longCounter.bind(Labels.of("key", "value"));

    boundLongCounter.add(100);

    verify(accumulator).recordLongAdd(instrumentKey, Labels.of("key", "value"), 100);
  }
}
