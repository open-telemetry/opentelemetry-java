/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.baggage.EntryMetadata.EntryTtl;
import org.junit.jupiter.api.Test;

class EntryMetadataTest {

  @Test
  void testGetEntryTtl() {
    EntryMetadata entryMetadata = EntryMetadata.create(EntryTtl.NO_PROPAGATION);
    assertThat(entryMetadata.getEntryTtl()).isEqualTo(EntryTtl.NO_PROPAGATION);
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EntryMetadata.create(EntryTtl.NO_PROPAGATION),
            EntryMetadata.create(EntryTtl.NO_PROPAGATION))
        .addEqualityGroup(EntryMetadata.create(EntryTtl.UNLIMITED_PROPAGATION))
        .testEquals();
  }
}
