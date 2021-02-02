/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class BaggageMetadataTest {

  @Test
  void getValue() {
    BaggageMetadata entryMetadata = BaggageMetadata.create("metadata;value=foo");
    assertThat(entryMetadata.getValue()).isEqualTo("metadata;value=foo");
  }

  @Test
  void nullValue() {
    assertThat(BaggageMetadata.create(null)).isEqualTo(BaggageMetadata.empty());
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(BaggageMetadata.create("value"), BaggageMetadata.create("value"))
        .addEqualityGroup(BaggageMetadata.create("other value"))
        .testEquals();
  }
}
