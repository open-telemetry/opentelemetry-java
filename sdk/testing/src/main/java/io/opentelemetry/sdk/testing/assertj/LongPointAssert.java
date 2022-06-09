/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Assertions for an exported {@link LongPointData}.
 *
 * @since 1.14.0
 */
public final class LongPointAssert extends AbstractPointAssert<LongPointAssert, LongPointData> {

  LongPointAssert(@Nullable LongPointData actual) {
    super(actual, LongPointAssert.class);
  }

  /** Asserts the point has the given value. */
  public LongPointAssert hasValue(long expected) {
    isNotNull();
    assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public LongPointAssert hasExemplars(LongExemplarData... exemplars) {
    isNotNull();
    assertThat(actual.getExemplars()).as("exemplars").containsExactlyInAnyOrder(exemplars);
    return myself;
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final LongPointAssert hasExemplarsSatisfying(Consumer<LongExemplarAssert>... assertions) {
    return hasExemplarsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  public LongPointAssert hasExemplarsSatisfying(
      Iterable<? extends Consumer<LongExemplarAssert>> assertions) {
    isNotNull();
    assertThat(actual.getExemplars())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, LongExemplarAssert::new));
    return myself;
  }
}
