/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.testing.assertj.AttributesAssert;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIterableAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link PointData}. */
public class AbstractPointDataAssert<
        PointAssertT extends AbstractPointDataAssert<PointAssertT, PointT>,
        PointT extends PointData>
    extends AbstractAssert<PointAssertT, PointT> {
  protected AbstractPointDataAssert(PointT actual, Class<PointAssertT> assertClass) {
    super(actual, assertClass);
  }

  /** Ensures the {@code start_epoch_nanos} field matches the expected value. */
  public PointAssertT hasStartEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getStartEpochNanos()).as("startEpochNanos").isEqualTo(expected);
    return myself;
  }

  /** Ensures the {@code epoch_nanos} field matches the expected value. */
  public PointAssertT hasEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return myself;
  }

  /** Ensures the {@code attributes} field matches the expected value. */
  public PointAssertT hasAttributes(Attributes expected) {
    isNotNull();
    Assertions.assertThat(actual.getAttributes()).as("attributes").isEqualTo(expected);
    return myself;
  }

  /** Returns convenience API to assert against the {@code attributes} field. */
  public AttributesAssert attributes() {
    isNotNull();
    return OpenTelemetryAssertions.assertThat(actual.getAttributes());
  }

  /** Returns convenience API to assert against the {@code exemplars} field. */
  public AbstractIterableAssert<?, ? extends Iterable<? extends ExemplarData>, ExemplarData, ?>
      exemplars() {
    isNotNull();
    return Assertions.assertThat(actual.getExemplars());
  }

  /**
   * Ensures the {@code exemplars} field matches the expected value.
   *
   * @param exemplars The list of exemplars that will be checked, can be in any order.
   */
  public PointAssertT hasExemplars(ExemplarData... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }
}
