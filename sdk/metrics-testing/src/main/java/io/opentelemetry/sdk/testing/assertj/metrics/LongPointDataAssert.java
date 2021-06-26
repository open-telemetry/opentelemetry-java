/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.LongPointData;
import org.assertj.core.api.Assertions;

/** Assertions for an exported {@link DoublePointData}. */
public class LongPointDataAssert
    extends AbstractPointDataAssert<LongPointDataAssert, LongPointData> {

  protected LongPointDataAssert(LongPointData actual) {
    super(actual, LongPointDataAssert.class);
  }

  public LongPointDataAssert hasValue(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getValue()).as("value").isEqualTo(expected);
    return this;
  }
}
