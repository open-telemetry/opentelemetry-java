/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.PointData;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link GaugeData}. */
public class GaugeAssert<T extends PointData> extends AbstractAssert<GaugeAssert<T>, GaugeData<T>> {
  protected GaugeAssert(GaugeData<T> actual) {
    super(actual, GaugeAssert.class);
  }

  public AbstractIterableAssert<?, ? extends Iterable<? extends T>, T, ?> points() {
    isNotNull();
    return Assertions.assertThat(actual.getPoints());
  }
}
