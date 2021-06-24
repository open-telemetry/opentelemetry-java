/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

public class DoubleSumDataAssert extends AbstractSumDataAssert<DoubleSumDataAssert, DoubleSumData> {
  protected DoubleSumDataAssert(DoubleSumData actual) {
    super(actual, DoubleSumDataAssert.class);
  }

  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends DoublePointData>, DoublePointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
