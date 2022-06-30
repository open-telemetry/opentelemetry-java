/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link SummaryData}.
 *
 * @since 1.14.0
 */
public final class SummaryAssert extends AbstractAssert<SummaryAssert, SummaryData> {

  SummaryAssert(SummaryData actual) {
    super(actual, SummaryAssert.class);
  }

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends SummaryPointData>, SummaryPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }

  /**
   * Asserts the summary has points matching all of the given assertions and no more, in any order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final SummaryAssert hasPointsSatisfying(Consumer<SummaryPointAssert>... assertions) {
    return hasPointsSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the summary has points matching all of the given assertions and no more, in any order.
   */
  public SummaryAssert hasPointsSatisfying(
      Iterable<? extends Consumer<SummaryPointAssert>> assertions) {
    assertThat(actual.getPoints())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, SummaryPointAssert::new));
    return this;
  }
}
