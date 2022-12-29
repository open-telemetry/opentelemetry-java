/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceConfigurationTest {

  @Test
  void configureResource() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(Collections.emptyMap()),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isNotNull();
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNotNull();
  }

  @Test
  void configureResource_EmptyClassLoader() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(Collections.emptyMap()),
                new URLClassLoader(new URL[0], null),
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
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(customConfigs),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_EnabledAndDisabled() {
    Map<String, String> customConfigs = new HashMap<>(2);
    customConfigs.put(
        "otel.java.enabled.resource.providers",
        "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider");
    customConfigs.put(
        "otel.java.disabled.resource.providers",
        "io.opentelemetry.sdk.extension.resources.TestColorResourceProvider");
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(customConfigs),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @Test
  void configureResource_OnlyDisabled() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put(
        "otel.java.disabled.resource.providers",
        "io.opentelemetry.sdk.autoconfigure.provider.TestColorResourceProvider");
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(customConfigs),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }
}
