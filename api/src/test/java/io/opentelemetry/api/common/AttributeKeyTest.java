package io.opentelemetry.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AttributeKeyTest {

  @Test
  void toStringIsKey() {
    assertThat(AttributeKey.longKey("hello").toString()).isEqualTo("hello");
  }
}
