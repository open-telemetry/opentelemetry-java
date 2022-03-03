/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceConfigurationTest {

  @Test
  void resource() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.get(Collections.emptyMap()),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNotNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE)).isNotNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_VERSION)).isNotNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_DESCRIPTION)).isNotNull();
  }

  @Test
  void emptyClassLoader() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.get(Collections.emptyMap()),
                new URLClassLoader(new URL[0], null),
                (r, c) -> r)
            .getAttributes();

    assertProcessAttributeIsNull(attributes);
  }

  @Test
  void onlyEnabledCustomResourceProvider() {
    Map<String, String> customConfigs = new HashMap<>(1);
    customConfigs.put("otel.java.enabled.resource.providers",
        "io.opentelemetry.sdk.autoconfigure.ResourceProviderCustomizer");
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.get(customConfigs),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r)
            .getAttributes();

    assertProcessAttributeIsNull(attributes);
    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isEqualTo("cat");
  }

  void assertProcessAttributeIsNull(Attributes attributes) {
    assertThat(attributes.get(ResourceAttributes.OS_TYPE)).isNull();
    assertThat(attributes.get(ResourceAttributes.OS_DESCRIPTION)).isNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_PID)).isNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_EXECUTABLE_PATH)).isNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_COMMAND_LINE)).isNull();

    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_NAME)).isNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_VERSION)).isNull();
    assertThat(attributes.get(ResourceAttributes.PROCESS_RUNTIME_DESCRIPTION)).isNull();
  }
}
