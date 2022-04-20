/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/** Assertions for an exported long {@link GaugeData}. */
public final class LongGaugeAssert
    extends AbstractAssert<LongGaugeAssert, GaugeData<LongPointData>> {

  LongGaugeAssert(@Nullable GaugeData<LongPointData> actual) {
    super(actual, LongGaugeAssert.class);
  }

  /**
   * Asserts the gauge has points matching all of the given assertions and no more, in any order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final LongGaugeAssert hasPointsSatisfying(Consumer<LongPointDataAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the gauge has points matching all of the given assertions and no more, in any order.
   */
  public LongGaugeAssert hasPointsSatisfying(
      Iterable<? extends Consumer<LongPointDataAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, LongPointDataAssert::new));
    return this;
  }
}
