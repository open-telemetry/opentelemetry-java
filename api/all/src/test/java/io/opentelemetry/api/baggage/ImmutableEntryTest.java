/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class ImmutableEntryTest {

  private static final String VALUE = "VALUE";
  private static final String VALUE_2 = "VALUE2";
  private static final BaggageEntryMetadata SAMPLE_METADATA =
      BaggageEntryMetadata.create("propagation=unlimited");

  @Test
  void testGetEntryMetadata() {
    assertThat(ImmutableEntry.create(VALUE, SAMPLE_METADATA).getMetadata())
        .isEqualTo(SAMPLE_METADATA);
  }

  @Test
  void testEntryEquals() {
    new EqualsTester()
        .addEqualityGroup(
            ImmutableEntry.create(VALUE, SAMPLE_METADATA),
            ImmutableEntry.create(VALUE, SAMPLE_METADATA))
        .addEqualityGroup(ImmutableEntry.create(VALUE_2, SAMPLE_METADATA))
        .addEqualityGroup(ImmutableEntry.create(VALUE, BaggageEntryMetadata.create("other")))
        .testEquals();
  }
}
