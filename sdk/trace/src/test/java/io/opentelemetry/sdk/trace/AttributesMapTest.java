/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.jupiter.api.Test;

class AttributesMapTest {

  @Test
  void asMap() {
    AttributesMap attributesMap = new AttributesMap(2, Integer.MAX_VALUE);
    attributesMap.put(longKey("one"), 1L);
    attributesMap.put(longKey("two"), 2L);

    assertThat(attributesMap.asMap())
        .containsOnly(entry(longKey("one"), 1L), entry(longKey("two"), 2L));
  }
}
