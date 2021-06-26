/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import org.assertj.core.api.Assertions;

/** Assertions for an exported {@link DoublePointData}. */
public class DoublePointDataAssert
    extends AbstractPointDataAssert<DoublePointDataAssert, DoublePointData> {

  protected DoublePointDataAssert(DoublePointData actual) {
    super(actual, DoublePointDataAssert.class);
  }

  public DoublePointDataAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
