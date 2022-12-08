/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import io.opentelemetry.api.common.AttributeKey;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;

class AttributeAssertionTest {

  @Test
  void nullAttr_errorMessageContainsAttrName() {
    AttributeKey<String> key = AttributeKey.stringKey("flib");
    AttributeAssertion attrAssertion = AttributeAssertion.create(key, AbstractAssert::isNotNull);
    AbstractAssert<?, ?> anAssert = AttributeAssertion.attributeValueAssertion(key, null);
    Consumer<AbstractAssert<?, ?>> a = attrAssertion.getAssertion();
    try {
      a.accept(anAssert);
      fail("Should have failed the assertion");
    } catch (AssertionError e) {
      assertThat(e).hasMessage("[STRING attribute 'flib'] \n" + "Expecting actual not to be null");
    }
  }
}
