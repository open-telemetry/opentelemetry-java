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

/**
 * Assertions for an exported long {@link GaugeData}.
 *
 * @since 1.14.0
 */
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
  public final LongGaugeAssert hasPointsSatisfying(Consumer<LongPointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the gauge has points matching all of the given assertions and no more, in any order.
   */
  public LongGaugeAssert hasPointsSatisfying(
      Iterable<? extends Consumer<LongPointAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, LongPointAssert::new));
    return this;
  }

  /**
   * Asserts that for each given assertion, at least one point in the gauge satisfies it. Extra
   * points that match none of the assertions are allowed, and a single point may satisfy multiple
   * assertions.
   *
   * @since 1.62.0
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final LongGaugeAssert containsPointsSatisfying(Consumer<LongPointAssert>... assertions) {
    return containsPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts that for each given assertion, at least one point in the gauge satisfies it. Extra
   * points that match none of the assertions are allowed, and a single point may satisfy multiple
   * assertions.
   *
   * @since 1.62.0
   */
  public LongGaugeAssert containsPointsSatisfying(
      Iterable<? extends Consumer<LongPointAssert>> assertions) {
    isNotNull();
    for (Consumer<LongPointAssert> assertion : assertions) {
      assertThat(actual.getPoints())
          .anySatisfy(point -> assertion.accept(new LongPointAssert(point)));
    }
    return this;
  }
}
