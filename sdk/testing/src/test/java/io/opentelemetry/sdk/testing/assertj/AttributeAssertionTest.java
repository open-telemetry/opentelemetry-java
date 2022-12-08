package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.api.common.AttributeKey;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.Test;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

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
      assertThat(e).hasMessage("[STRING attribute 'flib'] \n"
          + "Expecting actual not to be null");
    }

  }

}