/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link DoublePointData}. */
public class DoublePointDataAssert
    extends AbstractSampledPointDataAssert<DoublePointDataAssert, DoublePointData> {

  protected DoublePointDataAssert(DoublePointData actual) {
    super(actual, DoublePointDataAssert.class);
  }

  /** Ensures the {@code as_double} field matches the expected value. */
  public DoublePointDataAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
