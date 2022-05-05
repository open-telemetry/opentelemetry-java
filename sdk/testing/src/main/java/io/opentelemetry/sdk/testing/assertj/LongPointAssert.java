/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;

/** Assertions for an exported {@link LongPointData}. */
public final class LongPointAssert extends AbstractPointAssert<LongPointAssert, LongPointData> {

  LongPointAssert(@Nullable LongPointData actual) {
    super(actual, LongPointAssert.class);
  }

  /** Asserts the point has the given value. */
  public LongPointAssert hasValue(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public LongPointAssert hasExemplars(LongExemplarData... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }
}
