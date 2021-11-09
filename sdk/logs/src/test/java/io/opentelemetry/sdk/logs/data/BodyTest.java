/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BodyTest {

  @Test
  void stringValue() {
    Body value = Body.string("foobar");
    assertThat(value.asString()).isEqualTo("foobar");
    assertThat(value.getType()).isEqualTo(Body.Type.STRING);
    assertThat(value).isEqualTo(Body.string("foobar"));
  }

  @Test
  void emptyValue() {
    Body value = Body.empty();
    assertThat(value.asString()).isEqualTo("");
    assertThat(value.getType()).isEqualTo(Body.Type.EMPTY);
    assertThat(value).isEqualTo(Body.empty());
  }
}
