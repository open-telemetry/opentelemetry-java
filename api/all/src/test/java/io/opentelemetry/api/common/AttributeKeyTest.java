/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AttributeKeyTest {

  @Test
  @SuppressWarnings("AutoValueSubclassLeaked")
  void equalsVerifier() {
    EqualsVerifier.forClass(AutoValue_AttributeKeyImpl.class).verify();
  }

  @Test
  void nullToEmpty() {
    assertThat(AttributeKey.stringKey(null).getKey()).isEmpty();
  }
}
