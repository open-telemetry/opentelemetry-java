package io.opentelemetry.trace.attributes;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.doubleAttributeValue;

import io.opentelemetry.common.Attributes;
import org.junit.Test;

public class DoubleAttributeSetterTest {

  @Test
  public void attributesBuilder() {
    DoubleAttributeSetter setter = DoubleAttributeSetter.create("how much?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, 10.0);
    assertThat(attributes.build().get("how much?")).isEqualTo(doubleAttributeValue(10.0));
  }
}
