package io.opentelemetry.sdk.resources;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;

public class TestResourceProvider extends ResourceProvider {

  @Override
  protected Attributes getAttributes() {
    return Attributes.of(
        "providerAttribute",
        AttributeValue.longAttributeValue(42));
  }
}
