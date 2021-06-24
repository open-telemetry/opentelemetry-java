/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

public class LongSumDataAssert extends AbstractSumDataAssert<LongSumDataAssert, LongSumData> {
  protected LongSumDataAssert(LongSumData actual) {
    super(actual, LongSumDataAssert.class);
  }

  public AbstractIterableAssert<?, ? extends Iterable<? extends LongPointData>, LongPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
