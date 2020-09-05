/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace.attributes;

import static io.opentelemetry.common.AttributeValue.longAttributeValue;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Attributes;
import org.junit.jupiter.api.Test;

class LongAttributeSetterTest {

  @Test
  void attributesBuilder() {
    LongAttributeSetter setter = LongAttributeSetter.create("how much?");
    assertThat(setter.key()).isEqualTo("how much?");
    assertThat(setter.toString()).isEqualTo("how much?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, 10);
    assertThat(attributes.build().get("how much?")).isEqualTo(longAttributeValue(10));
  }
}
