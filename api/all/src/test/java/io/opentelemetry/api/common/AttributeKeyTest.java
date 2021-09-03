/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AttributeKeyTest {

  @Test
  void equalsVerifier() {
    EqualsVerifier.forClass(InternalAttributeKeyImpl.class)
        .withCachedHashCode(
            "hashCode", "buildHashCode", (InternalAttributeKeyImpl<?>) AttributeKey.stringKey("test"))
        .verify();
  }

  @Test
  void nullToEmpty() {
    assertThat(AttributeKey.stringKey(null).getKey()).isEmpty();
  }
}
