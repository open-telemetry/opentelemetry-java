/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceConfigurationFullTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(ResourceConfigurationFullTest.class.getClassLoader());

  @Test
  void configureResource_Default() {
    Attributes attributes = configureResource(Collections.emptyMap());

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isEqualTo("blue");
    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("cart");
  }

  @Test
  void configureResource_EmptyClassLoader() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.createFromMap(Collections.emptyMap()),
                SpiHelper.create(new URLClassLoader(new URL[0], null)),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isNull();
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_OnlyEnabled() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put(
        "otel.java.enabled.resource.providers",
        "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_EnabledAndDisabled() {
    Map<String, String> customConfigs = new HashMap<>(2);
    customConfigs.put(
        "otel.java.enabled.resource.providers",
        "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider");
    customConfigs.put("otel.resource.provider.color.enabled", "false");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_OnlyDisabled() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put("otel.resource.provider.color.enabled", "false");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_UserConfiguredService() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put("otel.resource.attributes", "service.name=user");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("user");
  }

  @Test
  void configureResource_UserConfiguredServiceUsingService() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put("otel.service.name", "user");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("user");
  }

  @Test
  void configureResource_UserConfiguredColor() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put("otel.resource.attributes", "color=red");
    Attributes attributes = configureResource(customConfigs);

    assertThat(attributes.get(AttributeKey.stringKey("color"))).isEqualTo("red");
  }

  private Attributes configureResource(Map<String, String> customConfigs) {
    return ResourceConfiguration.configureResource(
            DefaultConfigProperties.createFromMap(customConfigs), spiHelper, (r, c) -> r)
        .getAttributes();
  }
}
