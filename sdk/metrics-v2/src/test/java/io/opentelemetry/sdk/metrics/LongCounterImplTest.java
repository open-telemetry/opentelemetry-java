/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LongCounterImplTest {
  private final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create("test", "1.0");
  @Mock private Accumulator accumulator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void record_passthrough_noLabels() {
    InstrumentDescriptor expectedInstrumentDescriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    LongCounterImpl longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");

    longCounter.add(100);

    verify(accumulator)
        .recordLongAdd(
            instrumentationLibraryInfo, expectedInstrumentDescriptor, 100, Labels.empty());
  }

  @Test
  void record_passthrough_labels() {
    InstrumentDescriptor expectedInstrumentDescriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    LongCounterImpl longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");

    longCounter.add(100, Labels.of("key", "value"));

    verify(accumulator)
        .recordLongAdd(
            instrumentationLibraryInfo,
            expectedInstrumentDescriptor,
            100,
            Labels.of("key", "value"));
  }

  @Test
  void record_passthrough_boundLabels() {
    InstrumentDescriptor expectedInstrumentDescriptor =
        InstrumentDescriptor.create(
            "testCounter",
            "testDescription",
            "ms",
            InstrumentType.COUNTER,
            InstrumentValueType.LONG);

    LongCounter longCounter =
        new LongCounterImpl(
            accumulator, instrumentationLibraryInfo, "testCounter", "testDescription", "ms");
    LongCounter.BoundLongCounter boundLongCounter = longCounter.bind(Labels.of("key", "value"));

    boundLongCounter.add(100);

    verify(accumulator)
        .recordLongAdd(
            instrumentationLibraryInfo,
            expectedInstrumentDescriptor,
            100,
            Labels.of("key", "value"));
  }
}
