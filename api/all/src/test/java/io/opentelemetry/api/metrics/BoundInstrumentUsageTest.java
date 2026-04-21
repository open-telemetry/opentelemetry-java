/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates usage of bound instruments for a dice-rolling scenario.
 *
 * <p>When the full set of attribute combinations is known ahead of time — as it is here, with 6
 * fixed die faces — bound instruments eliminate the per-recording overhead of the CHM lookup
 * (bucket traversal, {@link io.opentelemetry.api.common.Attributes} equality comparison) and
 * attribute processing by resolving the underlying timeseries once at bind time.
 */
class BoundInstrumentUsageTest {

  private static final AttributeKey<Long> ROLL_VALUE = AttributeKey.longKey("roll.value");

  // One Attributes object per die face, constructed once and reused across all recordings.
  // With unbound instruments each call would construct (or look up) these on every add().
  private static final Attributes ROLL_1 = Attributes.of(ROLL_VALUE, 1L);
  private static final Attributes ROLL_2 = Attributes.of(ROLL_VALUE, 2L);
  private static final Attributes ROLL_3 = Attributes.of(ROLL_VALUE, 3L);
  private static final Attributes ROLL_4 = Attributes.of(ROLL_VALUE, 4L);
  private static final Attributes ROLL_5 = Attributes.of(ROLL_VALUE, 5L);
  private static final Attributes ROLL_6 = Attributes.of(ROLL_VALUE, 6L);

  @Test
  void rollTheDice() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {

      Meter meter = meterProvider.get("io.opentelemetry.example.dice");

      LongCounter rolls =
          meter
              .counterBuilder("dice.rolls")
              .setDescription("The number of times each side of the die was rolled")
              .setUnit("{roll}")
              .build();

      // Bind one LongCounterOp per die face. Each bind() call resolves the underlying timeseries
      // once, so subsequent add() calls record directly without any attribute lookup.
      //
      // Equivalent unbound setup (no bind calls needed, but per-recording overhead is higher):
      //   // no setup — just call rolls.add(1, ROLL_N) inline below
      LongCounterOp face1 = rolls.bind(ROLL_1);
      LongCounterOp face2 = rolls.bind(ROLL_2);
      LongCounterOp face3 = rolls.bind(ROLL_3);
      LongCounterOp face4 = rolls.bind(ROLL_4);
      LongCounterOp face5 = rolls.bind(ROLL_5);
      LongCounterOp face6 = rolls.bind(ROLL_6);

      // Simulate 600 rolls with a fixed seed for a reproducible distribution.
      Random random = new Random(42);
      long[] counts = new long[7]; // indexed 1..6; index 0 unused

      for (int i = 0; i < 600; i++) {
        int result = random.nextInt(6) + 1;
        counts[result]++;
        switch (result) {
          case 1:
            face1.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_1);
            break;
          case 2:
            face2.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_2);
            break;
          case 3:
            face3.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_3);
            break;
          case 4:
            face4.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_4);
            break;
          case 5:
            face5.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_5);
            break;
          case 6:
            face6.add(1);
            // Equivalent unbound: rolls.add(1, ROLL_6);
            break;
          default:
            break;
        }
      }

      // One cumulative data point per die face, each with the exact roll count recorded above.
      assertThat(reader.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasName("dice.rolls")
                      .hasDescription("The number of times each side of the die was rolled")
                      .hasUnit("{roll}")
                      .hasLongSumSatisfying(
                          sum ->
                              sum.isMonotonic()
                                  .hasPointsSatisfying(
                                      point -> point.hasAttributes(ROLL_1).hasValue(counts[1]),
                                      point -> point.hasAttributes(ROLL_2).hasValue(counts[2]),
                                      point -> point.hasAttributes(ROLL_3).hasValue(counts[3]),
                                      point -> point.hasAttributes(ROLL_4).hasValue(counts[4]),
                                      point -> point.hasAttributes(ROLL_5).hasValue(counts[5]),
                                      point -> point.hasAttributes(ROLL_6).hasValue(counts[6]))));
    }
  }
}
