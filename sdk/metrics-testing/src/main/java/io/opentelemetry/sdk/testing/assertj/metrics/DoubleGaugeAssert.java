/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link DoubleGaugeData}. */
public class DoubleGaugeAssert extends AbstractAssert<DoubleGaugeAssert, DoubleGaugeData> {
  protected DoubleGaugeAssert(DoubleGaugeData actual) {
    super(actual, DoubleGaugeAssert.class);
  }

  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends DoublePointData>, DoublePointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
