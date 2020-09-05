/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;

class BooleanAttributeSetterTest {

  @Test
  void attributesBuilder() {
    BooleanAttributeSetter setter = BooleanAttributeSetter.create("there?");
    assertThat(setter.key()).isEqualTo("there?");
    assertThat(setter.toString()).isEqualTo("there?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, true);
    assertThat(attributes.build().get("there?")).isEqualTo(booleanAttributeValue(true));
  }
}
