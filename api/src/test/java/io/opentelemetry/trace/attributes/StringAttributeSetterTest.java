/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

import static io.opentelemetry.common.AttributeValue.stringAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;

class StringAttributeSetterTest {

  @Test
  void attributesBuilder() {
    StringAttributeSetter setter = StringAttributeSetter.create("hello?");
    assertThat(setter.key()).isEqualTo("hello?");
    assertThat(setter.toString()).isEqualTo("hello?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, "world");
    assertThat(attributes.build().get("hello?")).isEqualTo(stringAttributeValue("world"));
  }
}
