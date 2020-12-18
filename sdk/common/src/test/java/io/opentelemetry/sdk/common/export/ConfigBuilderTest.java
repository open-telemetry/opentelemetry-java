/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.sdk.common.export.ConfigBuilder.NamingConvention;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

/** Tests for {@link io.opentelemetry.sdk.common.export.ConfigBuilder}. */
public class ConfigBuilderTest {

  @Test
  void normalize() {
    Map<String, String> dotValues =
        NamingConvention.DOT.normalize(Collections.singletonMap("Test.Config.Key", "value"));
    assertThat(dotValues).containsEntry("test.config.key", "value");

    Map<String, String> envValue =
        NamingConvention.ENV_VAR.normalize(Collections.singletonMap("TEST_CONFIG_KEY", "value"));
    assertThat(envValue).containsEntry("test.config.key", "value");
  }

  @Test
  void booleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "true"));
    assertThat(booleanProperty).isTrue();
  }

  @Test
  void longProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "42343"));
    assertThat(longProperty).isEqualTo(42343);
  }

  @Test
  void intProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "43543"));
    assertThat(intProperty).isEqualTo(43543);
  }

  @Test
  void doubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "5.6"));
    assertThat(doubleProperty).isEqualTo(5.6);
  }

  @Test
  void invalidBooleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "23435"));
    assertThat(booleanProperty).isFalse();
  }

  @Test
  void invalidLongProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "45.6"));
    assertThat(longProperty).isNull();
  }

  @Test
  void invalidIntProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "false"));
    assertThat(intProperty).isNull();
  }

  @Test
  void invalidDoubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "something"));
    assertThat(doubleProperty).isNull();
  }

  @Test
  void nullValue_BooleanProperty() {
    Boolean booleanProperty = ConfigBuilder.getBooleanProperty("boolean", Collections.emptyMap());
    assertThat(booleanProperty).isNull();
  }

  @Test
  void nullValue_LongProperty() {
    Long longProperty = ConfigBuilder.getLongProperty("long", Collections.emptyMap());
    assertThat(longProperty).isNull();
  }

  @Test
  void nullValue_IntProperty() {
    Integer intProperty = ConfigBuilder.getIntProperty("int", Collections.emptyMap());
    assertThat(intProperty).isNull();
  }

  @Test
  void nullValue_DoubleProperty() {
    Double doubleProperty = ConfigBuilder.getDoubleProperty("double", Collections.emptyMap());
    assertThat(doubleProperty).isNull();
  }

  @Test
  void testNormalize_dot() {
    assertThat(NamingConvention.DOT.normalize("lower.case")).isEqualTo("lower.case");
    assertThat(NamingConvention.DOT.normalize("lower_case")).isEqualTo("lower_case");
    assertThat(NamingConvention.DOT.normalize("loWer.cAsE")).isEqualTo("lower.case");
    assertThat(NamingConvention.DOT.normalize("loWer_cAsE")).isEqualTo("lower_case");
  }

  @Test
  void testNormalize_env() {
    assertThat(NamingConvention.ENV_VAR.normalize("lower.case")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("lower_case")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("loWer.cAsE")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("loWer_cAsE")).isEqualTo("lower.case");
  }

  @Test
  void testNormalize_dotMap() {
    Map<String, String> map = new HashMap<>();
    map.put("lower.case", "1");
    map.put("lower_case", "2");
    map.put("loWer.cAsE", "3");
    map.put("loWer_cAsE", "4");
    Map<String, String> normalized = NamingConvention.DOT.normalize(map);
    assertThat(normalized.size()).isEqualTo(2);
    assertThat(normalized).containsOnly(entry("lower.case", "3"), entry("lower_case", "4"));
  }

  @Test
  void testNormalize_envMap() {
    Map<String, String> map = new HashMap<>();
    map.put("lower.case", "1");
    map.put("lower_case", "2");
    map.put("loWer.cAsE", "3");
    map.put("loWer_cAsE", "4");
    Map<String, String> normalized = NamingConvention.ENV_VAR.normalize(map);
    assertThat(normalized.size()).isEqualTo(1);
    assertThat(normalized).containsExactly(entry("lower.case", "3"));
  }

  @Test
  void testBoolProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getBooleanProperty("int", map)).isFalse();
    assertThat(ConfigBuilder.getBooleanProperty("long", map)).isFalse();
    assertThat(ConfigBuilder.getBooleanProperty("boolt", map)).isTrue();
    assertThat(ConfigBuilder.getBooleanProperty("boolf", map)).isFalse();
    assertThat(ConfigBuilder.getBooleanProperty("string", map)).isFalse();
    assertThat(ConfigBuilder.getBooleanProperty("no-key", map)).isNull();
  }

  @Test
  void testIntProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getIntProperty("int", map)).isNotNull();
    assertThat(ConfigBuilder.getIntProperty("int", map)).isEqualTo(1);
    assertThat(ConfigBuilder.getIntProperty("long", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("boolt", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("boolf", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("string", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("no-key", map)).isNull();
  }

  @Test
  void testLongProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getLongProperty("int", map)).isNotNull();
    assertThat(ConfigBuilder.getLongProperty("int", map)).isEqualTo(1);
    assertThat(ConfigBuilder.getLongProperty("long", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("boolt", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("boolf", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("string", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("no-key", map)).isNull();
  }

  @Test
  void testStringProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getStringProperty("int", map)).isNotNull();
    assertThat(ConfigBuilder.getStringProperty("long", map)).isNotNull();
    assertThat(ConfigBuilder.getStringProperty("boolt", map)).isNotNull();
    assertThat(ConfigBuilder.getStringProperty("boolf", map)).isNotNull();
    assertThat(ConfigBuilder.getStringProperty("string", map)).isNotNull();
    assertThat(ConfigBuilder.getStringProperty("no-key", map)).isNull();
  }

  private static final class ConfigTester extends ConfigBuilder<Map<String, String>> {

    public static NamingConvention getNamingDot() {
      return NamingConvention.DOT;
    }

    @Override
    protected Map<String, String> fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      return configMap;
    }
  }

  @Nested
  @SuppressWarnings("ClassCanBeStatic")
  class ConfigurationSystemPropertiesTest {

    @Test
    @SetSystemProperty(key = "int", value = "1")
    @SetSystemProperty(key = "long", value = "2L")
    @SetSystemProperty(key = "boolt", value = "true")
    @SetSystemProperty(key = "boolf", value = "false")
    @SetSystemProperty(key = "string", value = "random")
    public void testSystemProperties() {
      ConfigTester config = new ConfigTester();
      Map<String, String> map = config.readSystemProperties();
      assertThat(ConfigBuilder.getStringProperty("int", map)).isEqualTo("1");
      assertThat(ConfigBuilder.getStringProperty("long", map)).isEqualTo("2L");
      assertThat(ConfigBuilder.getStringProperty("boolt", map)).isEqualTo("true");
      assertThat(ConfigBuilder.getStringProperty("boolf", map)).isEqualTo("false");
      assertThat(ConfigBuilder.getStringProperty("string", map)).isEqualTo("random");
      assertThat(ConfigBuilder.getStringProperty("no-key", map)).isNull();
    }
  }

  @Nested
  @SuppressWarnings("ClassCanBeStatic")
  class ConfigurationEnvVarsTest {

    @Test
    @SetEnvironmentVariable(key = "int", value = "1")
    @SetEnvironmentVariable(key = "long", value = "2L")
    @SetEnvironmentVariable(key = "boolt", value = "true")
    @SetEnvironmentVariable(key = "boolf", value = "false")
    @SetEnvironmentVariable(key = "string", value = "random")
    public void testEnvironmentVariables() {
      ConfigTester config = new ConfigTester();
      Map<String, String> map = config.readEnvironmentVariables();
      assertThat(ConfigBuilder.getStringProperty("int", map)).isEqualTo("1");
      assertThat(ConfigBuilder.getStringProperty("long", map)).isEqualTo("2L");
      assertThat(ConfigBuilder.getStringProperty("boolt", map)).isEqualTo("true");
      assertThat(ConfigBuilder.getStringProperty("boolf", map)).isEqualTo("false");
      assertThat(ConfigBuilder.getStringProperty("string", map)).isEqualTo("random");
      assertThat(ConfigBuilder.getStringProperty("no-key", map)).isNull();
    }
  }
}
