/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Assert on a {@link DoubleSummaryData} metric. */
public class DoubleSummaryDataAssert
    extends AbstractAssert<DoubleSummaryDataAssert, DoubleSummaryData> {

  protected DoubleSummaryDataAssert(DoubleSummaryData actual) {
    super(actual, DoubleSummaryDataAssert.class);
  }

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends DoubleSummaryPointData>, DoubleSummaryPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
