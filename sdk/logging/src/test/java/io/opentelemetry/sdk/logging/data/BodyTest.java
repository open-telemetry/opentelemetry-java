/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BodyTest {

  @Test
  void stringValue() {
    Body value = Body.stringBody("foobar");
    assertThat(value.asString()).isEqualTo("foobar");
    assertThat(value.getType()).isEqualTo(Body.Type.STRING);
  }
}
