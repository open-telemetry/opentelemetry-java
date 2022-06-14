/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for an exported double {@link GaugeData}.
 *
 * @since 1.14.0
 */
public final class DoubleGaugeAssert
    extends AbstractAssert<DoubleGaugeAssert, GaugeData<DoublePointData>> {

  DoubleGaugeAssert(@Nullable GaugeData<DoublePointData> actual) {
    super(actual, DoubleGaugeAssert.class);
  }

  /**
   * Asserts the gauge has points matching all of the given assertions and no more, in any order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final DoubleGaugeAssert hasPointsSatisfying(Consumer<DoublePointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the gauge has points matching all of the given assertions and no more, in any order.
   */
  public DoubleGaugeAssert hasPointsSatisfying(
      Iterable<? extends Consumer<DoublePointAssert>> assertions) {
    isNotNull();
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, DoublePointAssert::new));
    return this;
  }
}
