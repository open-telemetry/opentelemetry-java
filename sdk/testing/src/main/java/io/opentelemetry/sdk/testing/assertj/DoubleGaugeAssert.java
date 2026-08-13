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

  /**
   * Asserts that for each given assertion, at least one point in the gauge satisfies it. Extra
   * points that match none of the assertions are allowed, and a single point may satisfy multiple
   * assertions.
   *
   * @since 1.62.0
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final DoubleGaugeAssert containsPointsSatisfying(
      Consumer<DoublePointAssert>... assertions) {
    return containsPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts that for each given assertion, at least one point in the gauge satisfies it. Extra
   * points that match none of the assertions are allowed, and a single point may satisfy multiple
   * assertions.
   *
   * @since 1.62.0
   */
  public DoubleGaugeAssert containsPointsSatisfying(
      Iterable<? extends Consumer<DoublePointAssert>> assertions) {
    isNotNull();
    for (Consumer<DoublePointAssert> assertion : assertions) {
      assertThat(actual.getPoints())
          .anySatisfy(point -> assertion.accept(new DoublePointAssert(point)));
    }
    return this;
  }
}
