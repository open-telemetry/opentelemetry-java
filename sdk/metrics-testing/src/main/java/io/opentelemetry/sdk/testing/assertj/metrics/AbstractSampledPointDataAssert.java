/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.SampledPointData;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link SampledPointData}. */
public class AbstractSampledPointDataAssert<
        PointAssertT extends AbstractSampledPointDataAssert<PointAssertT, PointT>,
        PointT extends SampledPointData>
    extends AbstractPointDataAssert<PointAssertT, PointT> {
  protected AbstractSampledPointDataAssert(PointT actual, Class<PointAssertT> assertClass) {
    super(actual, assertClass);
  }

  /** Returns convenience API to assert against the {@code exemplars} field. */
  public AbstractIterableAssert<?, ? extends Iterable<? extends Exemplar>, Exemplar, ?>
      exemplars() {
    isNotNull();
    return Assertions.assertThat(actual.getExemplars());
  }

  /**
   * Ensures the {@code exemplars} field matches the expected value.
   *
   * @param exemplars The list of exemplars that will be checked, can be in any order.
   */
  public PointAssertT hasExemplars(Exemplar... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }
}
