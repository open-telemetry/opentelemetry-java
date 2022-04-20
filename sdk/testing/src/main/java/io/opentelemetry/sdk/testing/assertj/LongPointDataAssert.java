/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.LongPointData;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;

/** Assertions for an exported {@link LongPointData}. */
public final class LongPointDataAssert
    extends AbstractPointDataAssert<LongPointDataAssert, LongPointData> {

  LongPointDataAssert(@Nullable LongPointData actual) {
    super(actual, LongPointDataAssert.class);
  }

  /** Asserts the point has the given value. */
  public LongPointDataAssert hasValue(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
