package io.opentelemetry.trace.attributes;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import io.opentelemetry.common.Attributes;
import org.junit.Test;

public class StringAttributeSetterTest {

  @Test
  public void attributesBuilder() {
    StringAttributeSetter setter = StringAttributeSetter.create("hello?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, "world");
    assertThat(attributes.build().get("hello?")).isEqualTo(stringAttributeValue("world"));
  }
}
