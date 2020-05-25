/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.common.export;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.common.export.ConfigBuilder.NamingConvention;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link io.opentelemetry.sdk.common.export.ConfigBuilder}. */
@RunWith(JUnit4.class)
public class ConfigBuilderTest {

  @Test
  public void normalize() {
    Map<String, String> dotValues =
        NamingConvention.DOT.normalize(Collections.singletonMap("Test.Config.Key", "value"));
    assertThat(dotValues).containsEntry("test.config.key", "value");

    Map<String, String> envValue =
        NamingConvention.ENV_VAR.normalize(Collections.singletonMap("TEST_CONFIG_KEY", "value"));
    assertThat(envValue).containsEntry("test.config.key", "value");
  }

  @Test
  public void booleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "true"));
    assertThat(booleanProperty).isTrue();
  }

  @Test
  public void longProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "42343"));
    assertThat(longProperty).isEqualTo(42343);
  }

  @Test
  public void intProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "43543"));
    assertThat(intProperty).isEqualTo(43543);
  }

  @Test
  public void doubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "5.6"));
    assertThat(doubleProperty).isEqualTo(5.6);
  }

  @Test
  public void invalidBooleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.singletonMap("boolean", "23435"));
    assertThat(booleanProperty).isFalse();
  }

  @Test
  public void invalidLongProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.singletonMap("long", "45.6"));
    assertThat(longProperty).isNull();
  }

  @Test
  public void invalidIntProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.singletonMap("int", "false"));
    assertThat(intProperty).isNull();
  }

  @Test
  public void invalidDoubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.singletonMap("double", "something"));
    assertThat(doubleProperty).isNull();
  }

  @Test
  public void nullValue_BooleanProperty() {
    Boolean booleanProperty =
        ConfigBuilder.getBooleanProperty("boolean", Collections.<String, String>emptyMap());
    assertThat(booleanProperty).isNull();
  }

  @Test
  public void nullValue_LongProperty() {
    Long longProperty =
        ConfigBuilder.getLongProperty("long", Collections.<String, String>emptyMap());
    assertThat(longProperty).isNull();
  }

  @Test
  public void nullValue_IntProperty() {
    Integer intProperty =
        ConfigBuilder.getIntProperty("int", Collections.<String, String>emptyMap());
    assertThat(intProperty).isNull();
  }

  @Test
  public void nullValue_DoubleProperty() {
    Double doubleProperty =
        ConfigBuilder.getDoubleProperty("double", Collections.<String, String>emptyMap());
    assertThat(doubleProperty).isNull();
  }

  @Test
  public void testNormalize_dot() {
    assertThat(NamingConvention.DOT.normalize("lower.case")).isEqualTo("lower.case");
    assertThat(NamingConvention.DOT.normalize("lower_case")).isEqualTo("lower_case");
    assertThat(NamingConvention.DOT.normalize("loWer.cAsE")).isEqualTo("lower.case");
    assertThat(NamingConvention.DOT.normalize("loWer_cAsE")).isEqualTo("lower_case");
  }

  @Test
  public void testNormalize_env() {
    assertThat(NamingConvention.ENV_VAR.normalize("lower.case")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("lower_case")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("loWer.cAsE")).isEqualTo("lower.case");
    assertThat(NamingConvention.ENV_VAR.normalize("loWer_cAsE")).isEqualTo("lower.case");
  }

  @Test
  public void testNormalize_dotMap() {
    Map<String, String> map = new HashMap<>();
    map.put("lower.case", "1");
    map.put("lower_case", "2");
    map.put("loWer.cAsE", "3");
    map.put("loWer_cAsE", "4");
    Map<String, String> normalized = NamingConvention.DOT.normalize(map);
    assertThat(normalized.size()).isEqualTo(2);
    assertThat(normalized.get("lower.case")).isNotEmpty();
    assertThat(normalized.get("lower_case")).isNotEmpty();
  }

  @Test
  public void testNormalize_envMap() {
    Map<String, String> map = new HashMap<>();
    map.put("lower.case", "1");
    map.put("lower_case", "2");
    map.put("loWer.cAsE", "3");
    map.put("loWer_cAsE", "4");
    Map<String, String> normalized = NamingConvention.ENV_VAR.normalize(map);
    assertThat(normalized.size()).isEqualTo(1);
    assertThat(normalized.get("lower.case")).isNotEmpty();
  }

  @Test
  public void testBoolProperty() {
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
  public void testIntProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getIntProperty("int", map)).isNotNull();
    assertThat(ConfigBuilder.getIntProperty("long", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("boolt", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("boolf", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("string", map)).isNull();
    assertThat(ConfigBuilder.getIntProperty("no-key", map)).isNull();
  }

  @Test
  public void testLongProperty() {
    Map<String, String> map = new HashMap<>();
    map.put("int", "1");
    map.put("long", "2L");
    map.put("boolt", "true");
    map.put("boolf", "false");
    map.put("string", "random");
    assertThat(ConfigBuilder.getLongProperty("int", map)).isNotNull();
    assertThat(ConfigBuilder.getLongProperty("long", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("boolt", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("boolf", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("string", map)).isNull();
    assertThat(ConfigBuilder.getLongProperty("no-key", map)).isNull();
  }

  @Test
  public void testStringProperty() {
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

    @Override
    protected Map<String, String> fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      return configMap;
    }
  }

  @RunWith(JUnit4.class)
  public static class ConfigurationSystemPropertiesTest {
    @Rule
    public final ProvideSystemProperty systemProperties =
        new ProvideSystemProperty("int", "1")
            .and("long", "2L")
            .and("boolt", "true")
            .and("boolf", "false")
            .and("string", "random");

    @Test
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

  @RunWith(JUnit4.class)
  public static class ConfigurationEnvVarsTest {
    @Rule
    public final EnvironmentVariables environmentVariables =
        new EnvironmentVariables()
            .set("int", "1")
            .set("long", "2L")
            .set("boolt", "true")
            .set("boolf", "false")
            .set("string", "random");

    @Test
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
