package io.opentelemetry.trace.attributes;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.booleanAttributeValue;

import io.opentelemetry.common.Attributes;
import org.junit.Test;

public class BooleanAttributeSetterTest {

  @Test
  public void attributesBuilder() {
    BooleanAttributeSetter setter = BooleanAttributeSetter.create("there?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, true);
    assertThat(attributes.build().get("there?")).isEqualTo(booleanAttributeValue(true));
  }
}
