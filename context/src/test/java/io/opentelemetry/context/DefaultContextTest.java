/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DefaultContextTest {

  @Test
  void hashcodeCollidingKeys() {
    DefaultContext context = new DefaultContext();
    HashCollidingKey cheese = new HashCollidingKey();
    HashCollidingKey wine = new HashCollidingKey();

    Context twoKeys = context.with(cheese, "whiz").with(wine, "boone's farm");

    assertThat(twoKeys.get(wine)).isEqualTo("boone's farm");
    assertThat(twoKeys.get(cheese)).isEqualTo("whiz");
  }

  private static class HashCollidingKey implements ContextKey<String> {
    @Override
    public int hashCode() {
      return 1;
    }
  }
}
