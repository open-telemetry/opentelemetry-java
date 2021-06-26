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

public class AbstractPointDataAssert<
        PointAssertT extends AbstractPointDataAssert<PointAssertT, PointT>,
        PointT extends PointData>
    extends AbstractAssert<PointAssertT, PointT> {
  protected AbstractPointDataAssert(PointT actual, Class<PointAssertT> assertClass) {
    super(actual, assertClass);
  }

  public PointAssertT hasStartEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getStartEpochNanos()).as("startEpochNanos").isEqualTo(expected);
    return myself;
  }

  public PointAssertT hasEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return myself;
  }

  public PointAssertT hasAttributes(Attributes expected) {
    isNotNull();
    Assertions.assertThat(actual.getAttributes()).as("attributes").isEqualTo(expected);
    return myself;
  }

  public AttributesAssert attributes() {
    isNotNull();
    return OpenTelemetryAssertions.assertThat(actual.getAttributes());
  }
}
