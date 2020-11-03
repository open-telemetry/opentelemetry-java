/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class EntryMetadataTest {

  @Test
  void getValue() {
    EntryMetadata entryMetadata = EntryMetadata.create("metadata;value=foo");
    assertThat(entryMetadata.getValue()).isEqualTo("metadata;value=foo");
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(EntryMetadata.create("value"), EntryMetadata.create("value"))
        .addEqualityGroup(EntryMetadata.create("other value"))
        .testEquals();
  }
}
