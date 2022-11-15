/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link ValueAtQuantile}.
 *
 * @since 1.14.0
 */
public final class ValueAtQuantileAssert
    extends AbstractAssert<ValueAtQuantileAssert, ValueAtQuantile> {

  ValueAtQuantileAssert(ValueAtQuantile actual) {
    super(actual, ValueAtQuantileAssert.class);
  }

  /** Asserts the given quantile. */
  public ValueAtQuantileAssert hasQuantile(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getQuantile()).as("quantile").isEqualTo(expected);
    return this;
  }

  /** Asserts the given value. */
  public ValueAtQuantileAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
