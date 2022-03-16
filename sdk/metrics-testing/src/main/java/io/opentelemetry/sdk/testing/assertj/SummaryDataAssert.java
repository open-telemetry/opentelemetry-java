/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Assert on a {@link SummaryData} metric. */
public class SummaryDataAssert extends AbstractAssert<SummaryDataAssert, SummaryData> {

  protected SummaryDataAssert(SummaryData actual) {
    super(actual, SummaryDataAssert.class);
  }

  /** Returns convenience API to assert against the {@code points} field. */
  public AbstractIterableAssert<
          ?, ? extends Iterable<? extends SummaryPointData>, SummaryPointData, ?>
      points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
