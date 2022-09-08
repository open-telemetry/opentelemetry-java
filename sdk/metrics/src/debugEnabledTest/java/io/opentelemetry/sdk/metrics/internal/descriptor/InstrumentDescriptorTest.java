/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.InstrumentValueType;
import org.junit.jupiter.api.Test;

/**
 * {@link InstrumentDescriptor#equals(Object)} must ignore {@link
 * InstrumentDescriptor#getSourceInfo()}, which only returns a meaningful value when {@code
 * otel.experimental.sdk.metrics.debug=true}.
 */
class InstrumentDescriptorTest {

  @Test
  void equals() {
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG);

    assertThat(descriptor)
        .isEqualTo(
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));

    // Validate getSourceInfo() is not equal for otherwise equal descriptors
    assertThat(descriptor.getSourceInfo())
        .isNotEqualTo(
            InstrumentDescriptor.create(
                    "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG)
                .getSourceInfo());

    // Validate that name, description, unit, type, and value type are considered in equals
    assertThat(descriptor)
        .isNotEqualTo(
            InstrumentDescriptor.create(
                "foo", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));
    assertThat(descriptor)
        .isNotEqualTo(
            InstrumentDescriptor.create(
                "name", "foo", "unit", InstrumentType.COUNTER, InstrumentValueType.LONG));
    assertThat(descriptor)
        .isNotEqualTo(
            InstrumentDescriptor.create(
                "name", "description", "foo", InstrumentType.COUNTER, InstrumentValueType.LONG));
    assertThat(descriptor)
        .isNotEqualTo(
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.UP_DOWN_COUNTER,
                InstrumentValueType.LONG));
    assertThat(descriptor)
        .isNotEqualTo(
            InstrumentDescriptor.create(
                "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE));
  }
}
