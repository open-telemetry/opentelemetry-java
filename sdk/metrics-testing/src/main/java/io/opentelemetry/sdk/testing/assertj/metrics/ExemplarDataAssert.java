/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.testing.assertj.AttributesAssert;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link ExemplarData}. */
public class ExemplarDataAssert extends AbstractAssert<ExemplarDataAssert, ExemplarData> {
  protected ExemplarDataAssert(ExemplarData actual) {
    super(actual, ExemplarDataAssert.class);
  }

  /** Ensures the {@code epochNanos} field matches the expected value. */
  public ExemplarDataAssert hasEpochNanos(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getEpochNanos()).as("epochNanos").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code spanId} field matches the expected value. */
  public ExemplarDataAssert hasSpanId(String expected) {
    isNotNull();
    Assertions.assertThat(actual.getSpanId()).as("spanId").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code traceId} field matches the expected value. */
  public ExemplarDataAssert hasTraceId(String expected) {
    isNotNull();
    Assertions.assertThat(actual.getTraceId()).as("traceId").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code value} field matches the expected value. */
  public ExemplarDataAssert hasValue(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getValueAsDouble()).as("value").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code fitleredAttributes} field matches the expected value. */
  public ExemplarDataAssert hasFilteredAttributes(Attributes expected) {
    isNotNull();
    Assertions.assertThat(actual.getFilteredAttributes())
        .as("filtered_attributes")
        .isEqualTo(expected);
    return this;
  }

  /** Returns convenience API to assert against the {@code filteredAttributes} field. */
  public AttributesAssert filteredAttributes() {
    isNotNull();
    return OpenTelemetryAssertions.assertThat(actual.getFilteredAttributes());
  }
}
