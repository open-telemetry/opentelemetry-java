/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttributeKeyTest {

  @Test
  void toStringIsKey() {
    assertThat(AttributeKey.longKey("hello").toString()).isEqualTo("hello");
  }
}
