/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class BaggageEntryMetadataTest {

  @Test
  void getValue() {
    BaggageEntryMetadata entryMetadata = BaggageEntryMetadata.create("metadata;value=foo");
    assertThat(entryMetadata.getValue()).isEqualTo("metadata;value=foo");
  }

  @Test
  void nullValue() {
    assertThat(BaggageEntryMetadata.create(null)).isEqualTo(BaggageEntryMetadata.empty());
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            BaggageEntryMetadata.create("value"), BaggageEntryMetadata.create("value"))
        .addEqualityGroup(BaggageEntryMetadata.create("other value"))
        .testEquals();
  }
}
