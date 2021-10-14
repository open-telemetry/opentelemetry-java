/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.PropertySource;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

class DefaultConfigPropertiesTest {

  @Test
  void readsPropertySource() {
    ConfigProperties config = DefaultConfigProperties.get();
    assertThat(config.getString("animal")).isEqualTo("cat");
    assertThat(config.getString("programming.language")).isEqualTo("java");
    assertThat(config.getString("unknown")).isNull();
  }

  @Test
  @SetEnvironmentVariable(key = "PROGRAMMING_LANGUAGE", value = "cobol")
  void envOverrides() {
    ConfigProperties config = DefaultConfigProperties.get();
    assertThat(config.getString("animal")).isEqualTo("cat");
    assertThat(config.getString("programming.language")).isEqualTo("cobol");
    assertThat(config.getString("unknown")).isNull();
  }

  @Test
  @SetSystemProperty(key = "programming-language", value = "cobol")
  void propOverrides() {
    ConfigProperties config = DefaultConfigProperties.get();
    assertThat(config.getString("animal")).isEqualTo("cat");
    assertThat(config.getString("programming.language")).isEqualTo("cobol");
    assertThat(config.getString("unknown")).isNull();
  }

  public static class TestPropertySource implements PropertySource {
    @Override
    public Map<String, String> getProperties() {
      Map<String, String> props = new HashMap<>();
      props.put("animal", "cat");
      props.put("programming-language", "java");
      return props;
    }
  }
}
