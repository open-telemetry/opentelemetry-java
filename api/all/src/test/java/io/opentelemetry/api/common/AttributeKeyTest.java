/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AttributeKeyTest {

  @Test
  @SuppressWarnings("AutoValueSubclassLeaked")
  void equalsVerifier() {
    EqualsVerifier.forClass(AutoValue_AttributeKeyImpl.class).verify();
  }
}
