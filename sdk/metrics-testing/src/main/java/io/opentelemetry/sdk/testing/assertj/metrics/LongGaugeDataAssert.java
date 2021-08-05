/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link LongGaugeData}. */
public class LongGaugeDataAssert extends AbstractAssert<LongGaugeDataAssert, LongGaugeData> {
  protected LongGaugeDataAssert(LongGaugeData actual) {
    super(actual, LongGaugeDataAssert.class);
  }

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<?, ? extends Iterable<? extends LongPointData>, LongPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
