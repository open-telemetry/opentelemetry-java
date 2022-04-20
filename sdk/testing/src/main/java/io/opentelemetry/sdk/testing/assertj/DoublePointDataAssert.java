/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import javax.annotation.Nullable;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link DoublePointData}. */
public final class DoublePointDataAssert
    extends AbstractPointDataAssert<DoublePointDataAssert, DoublePointData> {

  DoublePointDataAssert(@Nullable DoublePointData actual) {
    super(actual, DoublePointDataAssert.class);
  }

  /** Asserts the point has the given value. */
  public DoublePointDataAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
