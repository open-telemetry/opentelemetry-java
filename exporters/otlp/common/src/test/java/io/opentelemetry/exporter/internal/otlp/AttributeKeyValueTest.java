/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AttributeKeyValueTest {

  @Test
  void equalsVerifier() {
    EqualsVerifier.forClass(AttributeKeyValue.class).verify();
  }

  @Test
  void ofEmpty() {
    assertThat(AttributeKeyValue.of(Attributes.empty())).isEmpty();
  }

  @Test
  void ofOne() {
    AttributeKeyValue<String> input = AttributeKeyValue.of(AttributeKey.stringKey("foo"), "bar");
    Attributes attributes = Attributes.of(input.getAttributeKey(), input.getValue());
    List<AttributeKeyValue<?>> list = AttributeKeyValue.of(attributes);
    Assertions.assertThat(list).hasSize(1);
    assertThat(list.get(0)).isEqualTo(input);
  }

  @Test
  void ofList() {
    AttributeKeyValue<List<Long>> input =
        AttributeKeyValue.of(AttributeKey.longArrayKey("foo"), Collections.emptyList());
    Attributes attributes = Attributes.of(input.getAttributeKey(), input.getValue());
    List<AttributeKeyValue<?>> list = AttributeKeyValue.of(attributes);
    Assertions.assertThat(list).hasSize(1);
    assertThat(list.get(0).getAttributeKey().getType()).isEqualTo(AttributeType.LONG_ARRAY);
  }
}
