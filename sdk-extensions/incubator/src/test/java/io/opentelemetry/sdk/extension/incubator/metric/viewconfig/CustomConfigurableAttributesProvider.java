package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricAttributesProvider;


public class CustomConfigurableAttributesProvider implements ConfigurableMetricAttributesProvider {
  @Override
  public Attributes addCustomAttributes(ConfigProperties config) {
    return Attributes.of(AttributeKey.stringKey("foo"),"val");
  }
}
