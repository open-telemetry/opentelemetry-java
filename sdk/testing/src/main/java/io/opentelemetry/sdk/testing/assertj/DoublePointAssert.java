/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link DoublePointData}.
 *
 * @since 1.14.0
 */
public final class DoublePointAssert
    extends AbstractPointAssert<DoublePointAssert, DoublePointData> {

  DoublePointAssert(@Nullable DoublePointData actual) {
    super(actual, DoublePointAssert.class);
  }

  /** Asserts the point has the given value. */
  public DoublePointAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public DoublePointAssert hasExemplars(DoubleExemplarData... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final DoublePointAssert hasExemplarsSatisfying(
      Consumer<DoubleExemplarAssert>... assertions) {
    return hasExemplarsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  public DoublePointAssert hasExemplarsSatisfying(
      Iterable<? extends Consumer<DoubleExemplarAssert>> assertions) {
    isNotNull();
    assertThat(actual.getExemplars())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, DoubleExemplarAssert::new));
    return myself;
  }
}
