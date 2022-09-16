/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultConfigPropertiesTest {

  @Test
  void resourceAttributesMerged() {
    Map<String, String> environment = new HashMap<>();
    Map<String, String> system = new HashMap<>();

    environment.put(
        "OTEL_RESOURCE_ATTRIBUTES", "deployment.environment=env,service.name=env.service");
    system.put("otel.resource.attributes", "deployment.environment=sys");
    String expected =
        "deployment.environment=env,service.name=env.service,deployment.environment=sys";

    ConfigProperties props = DefaultConfigProperties.createForTest(system, environment);

    assertThat(props.getString("otel.resource.attributes")).isEqualTo(expected);
  }

  @Test
  void resourceAttributesMergedWhenEnvEmpty() {
    Map<String, String> environment = new HashMap<>();
    Map<String, String> system = new HashMap<>();

    environment.put("OTEL_RESOURCE_ATTRIBUTES", " ");
    system.put("otel.resource.attributes", "deployment.environment=sys");
    String expected = "deployment.environment=sys";

    ConfigProperties props = DefaultConfigProperties.createForTest(system, environment);

    assertThat(props.getString("otel.resource.attributes")).isEqualTo(expected);
  }

  @Test
  void resourceAttributesEmptySyspropCanClobber() {
    Map<String, String> environment = new HashMap<>();
    Map<String, String> system = new HashMap<>();

    environment.put("OTEL_RESOURCE_ATTRIBUTES", "service.name=foo,deployment.environment=blah");
    system.put("otel.resource.attributes", "deployment.environment=");
    String expected = "service.name=foo,deployment.environment=blah,deployment.environment=";

    ConfigProperties props = DefaultConfigProperties.createForTest(system, environment);

    assertThat(props.getString("otel.resource.attributes")).isEqualTo(expected);
  }
}
