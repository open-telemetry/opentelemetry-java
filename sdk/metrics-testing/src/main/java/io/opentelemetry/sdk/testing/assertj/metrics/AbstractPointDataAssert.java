/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.testing.assertj.AttributesAssert;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import org.assertj.core.api.AbstractAssert;
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
}
