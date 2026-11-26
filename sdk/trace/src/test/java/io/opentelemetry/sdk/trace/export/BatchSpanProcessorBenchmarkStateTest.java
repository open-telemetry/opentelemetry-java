/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessorBenchmarkState.Counters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchSpanProcessorBenchmarkStateTest {
  private final BatchSpanProcessorBenchmarkState state = new BatchSpanProcessorBenchmarkState();

  @BeforeEach
  void setUp() {
    state.configureLogging();
    state.maxQueueSize = 16;
    state.maxExportBatchSize = 2;
    state.setup();
  }

  @AfterEach
  void tearDown() {
    try {
      state.close();
    } finally {
      state.restoreLogging();
    }
  }

  @Test
  void drainsAndPublishesEverySpanOnceAcrossProducers() {
    Counters first = new Counters();
    Counters second = new Counters();
    Counters third = new Counters();
    first.register(state, 0);
    second.register(state, 1);
    third.register(state, 2);
    for (int i = 0; i < 3; i++) {
      state.endSpan(first);
    }
    state.endSpan(second);
    state.endSpan(third);

    state.finishIteration();

    assertThat(state.attemptedSpans).isEqualTo(5);
    assertThat(first.exportedSpans() + second.exportedSpans() + third.exportedSpans()).isEqualTo(5);
    assertThat(state.droppedSpans).isZero();
    assertThat(first.elapsedNanos()).isPositive();
    assertThat(second.elapsedNanos()).isZero();
    assertThat(third.elapsedNanos()).isZero();
  }

  @Test
  void rejectsIncompleteAccounting() {
    Counters producer = new Counters();
    producer.register(state, 0);
    state.endSpan(producer);
    producer.attemptedSpans++;

    assertThatThrownBy(state::finishIteration)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Incomplete benchmark accounting");
  }

  @Test
  void aggregatesDistinctMetricPointsWithoutDividingOrLosingRemainders() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meters =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {
      LongCounter counter = meters.get("test").counterBuilder("processedSpans").build();
      counter.add(3, Attributes.builder().put("dropped", false).put("processor", "first").build());
      counter.add(4, Attributes.builder().put("dropped", false).put("processor", "second").build());
      counter.add(2, Attributes.builder().put("dropped", true).put("processor", "first").build());

      BatchSpanProcessorMetrics metrics = new BatchSpanProcessorMetrics(reader.collectAllMetrics());

      assertThat(metrics.exportedSpans()).isEqualTo(7);
      assertThat(metrics.droppedSpans()).isEqualTo(2);
    }
  }
}
