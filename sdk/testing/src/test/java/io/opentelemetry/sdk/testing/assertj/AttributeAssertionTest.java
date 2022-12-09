/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;

class AttributeAssertionTest {

  @Test
  void nullAttr_errorMessageContainsAttrName() {
    AttributeKey<String> key = AttributeKey.stringKey("flib");

    assertThatThrownBy(
            () ->
                AttributeAssertion.create(key, AbstractAssert::isNotNull)
                    .getAssertion()
                    .accept(AttributeAssertion.attributeValueAssertion(key, null)))
        .isInstanceOf(AssertionError.class)
        .hasMessage("[STRING attribute 'flib'] \nExpecting actual not to be null");
  }
}
