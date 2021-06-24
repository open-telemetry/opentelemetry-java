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

public class LongGaugeDataAssert extends AbstractAssert<LongGaugeDataAssert, LongGaugeData> {
  protected LongGaugeDataAssert(LongGaugeData actual) {
    super(actual, LongGaugeDataAssert.class);
  }

  public AbstractIterableAssert<?, ? extends Iterable<? extends LongPointData>, LongPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
