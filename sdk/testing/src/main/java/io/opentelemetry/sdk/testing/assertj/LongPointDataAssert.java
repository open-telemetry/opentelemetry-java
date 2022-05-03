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

/** Assertions for an exported {@link LongPointData}. */
public final class LongPointDataAssert
    extends AbstractPointDataAssert<LongPointDataAssert, LongPointData> {

  LongPointDataAssert(@Nullable LongPointData actual) {
    super(actual, LongPointDataAssert.class);
  }

  /** Asserts the point has the given value. */
  public LongPointDataAssert hasValue(long expected) {
    isNotNull();
    assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public LongPointDataAssert hasExemplars(LongExemplarData... exemplars) {
    isNotNull();
    assertThat(actual.getExemplars()).as("exemplars").containsExactlyInAnyOrder(exemplars);
    return myself;
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final LongPointDataAssert hasExemplarsSatisfying(
      Consumer<LongExemplarAssert>... assertions) {
    return hasExemplarsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  public LongPointDataAssert hasExemplarsSatisfying(
      Iterable<? extends Consumer<LongExemplarAssert>> assertions) {
    isNotNull();
    assertThat(actual.getExemplars())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, LongExemplarAssert::new));
    return myself;
  }
}
