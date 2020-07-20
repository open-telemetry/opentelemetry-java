package io.opentelemetry.trace.attributes;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.longAttributeValue;

import io.opentelemetry.common.Attributes;
import org.junit.Test;

public class LongAttributeSetterTest {

  @Test
  public void attributesBuilder() {
    LongAttributeSetter setter = LongAttributeSetter.create("how much?");
    Attributes.Builder attributes = Attributes.newBuilder();
    setter.set(attributes, 10);
    assertThat(attributes.build().get("how much?")).isEqualTo(longAttributeValue(10));
  }
}
