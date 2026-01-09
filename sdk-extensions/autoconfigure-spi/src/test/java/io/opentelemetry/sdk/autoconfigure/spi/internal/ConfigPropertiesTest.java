/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigPropertiesTest {

  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(ConfigPropertiesTest.class.getClassLoader());

  @Test
  void allValid() {
    Map<String, String> properties = makeTestProps();

    ConfigProperties config = DefaultConfigProperties.createFromMap(properties);
    assertThat(config.getString("test.string")).isEqualTo("str");
    assertThat(config.getInt("test.int")).isEqualTo(10);
    assertThat(config.getLong("test.long")).isEqualTo(20);
    assertThat(config.getDouble("test.double")).isEqualTo(5.4);
    assertThat(config.getList("test.list")).containsExactly("cat", "dog", "bear");
    assertThat(config.getMap("test.map"))
        .containsExactly(entry("cat", "meow"), entry("dog", "bark"), entry("bear", "growl"));
    assertThat(config.getDuration("test.duration")).isEqualTo(Duration.ofSeconds(1));
  }

  @Test
  void allValidUsingHyphens() {
    Map<String, String> properties = makeTestProps();

    ConfigProperties config = DefaultConfigProperties.createFromMap(properties);
    assertThat(config.getString("test-string")).isEqualTo("str");
    assertThat(config.getInt("test-int")).isEqualTo(10);
    assertThat(config.getLong("test-long")).isEqualTo(20);
    assertThat(config.getDouble("test-double")).isEqualTo(5.4);
    assertThat(config.getList("test-list")).containsExactly("cat", "dog", "bear");
    assertThat(config.getMap("test-map"))
        .containsExactly(entry("cat", "meow"), entry("dog", "bark"), entry("bear", "growl"));
    assertThat(config.getDuration("test-duration")).isEqualTo(Duration.ofSeconds(1));
  }

  @Test
  void allMissing() {
    ConfigProperties config = DefaultConfigProperties.createFromMap(emptyMap());
    assertThat(config.getString("test.string")).isNull();
    assertThat(config.getInt("test.int")).isNull();
    assertThat(config.getLong("test.long")).isNull();
    assertThat(config.getDouble("test.double")).isNull();
    assertThat(config.getList("test.list")).isEmpty();
    assertThat(config.getMap("test.map")).isEmpty();
    assertThat(config.getDuration("test.duration")).isNull();
  }

  @Test
  void allEmpty() {
    Map<String, String> properties = new HashMap<>();
    properties.put("test.string", "");
    properties.put("test.int", "");
    properties.put("test.long", "");
    properties.put("test.double", "");
    properties.put("test.list", "");
    properties.put("test.map", "");
    properties.put("test.duration", "");

    ConfigProperties config = DefaultConfigProperties.createFromMap(properties);
    assertThat(config.getString("test.string")).isEmpty();
    assertThat(config.getInt("test.int")).isNull();
    assertThat(config.getLong("test.long")).isNull();
    assertThat(config.getDouble("test.double")).isNull();
    assertThat(config.getList("test.list")).isEmpty();
    assertThat(config.getMap("test.map")).isEmpty();
    assertThat(config.getDuration("test.duration")).isNull();
  }

  @Test
  void invalidInt() {
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("int", "bar"))
                    .getInt("int"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property int=bar. Must be a integer.");
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(
                        Collections.singletonMap("int", "999999999999999"))
                    .getInt("int"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property int=999999999999999. Must be a integer.");
  }

  @Test
  void invalidLong() {
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("long", "bar"))
                    .getLong("long"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property long=bar. Must be a long.");
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(
                        Collections.singletonMap("long", "99223372036854775807"))
                    .getLong("long"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property long=99223372036854775807. Must be a long.");
  }

  @Test
  void invalidDouble() {
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("double", "bar"))
                    .getDouble("double"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property double=bar. Must be a double.");
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("double", "1.0.1"))
                    .getDouble("double"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid value for property double=1.0.1. Must be a double.");
  }

  @Test
  void uncleanList() {
    assertThat(
            DefaultConfigProperties.createFromMap(
                    Collections.singletonMap("list", "  a  ,b,c  ,  d,,   ,"))
                .getList("list"))
        .containsExactly("a", "b", "c", "d");
  }

  @Test
  void uncleanMap() {
    assertThat(
            DefaultConfigProperties.createFromMap(
                    Collections.singletonMap("map", "  a=1  ,b=2,c = 3  ,  d=  4,,  ,"))
                .getMap("map"))
        .containsExactly(entry("a", "1"), entry("b", "2"), entry("c", "3"), entry("d", "4"));
  }

  @Test
  void invalidMap() {
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("map", "a=1,b"))
                    .getMap("map"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid map property: map=a=1,b");
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("map", "a=1,=b"))
                    .getMap("map"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid map property: map=a=1,=b");
  }

  @Test
  void invalidDuration() {
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "1a1ms"))
                    .getDuration("duration"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid duration property duration=1a1ms. Expected number, found: 1a1");
    assertThatThrownBy(
            () ->
                DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "9mm"))
                    .getDuration("duration"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid duration property duration=9mm. Invalid duration string, found: mm");
  }

  @Test
  void durationNegativeParsing() {
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "-41"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMillis(-41));
  }

  @Test
  void durationUnitParsing() {
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "3ns"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofNanos(3));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "2us"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofNanos(2000));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "1"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMillis(1));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "2ms"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMillis(2));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "3s"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofSeconds(3));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "4m"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMinutes(4));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "5h"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofHours(5));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "6d"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofDays(6));
    // Check Space handling
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "7 ms"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMillis(7));
    assertThat(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("duration", "8   ms"))
                .getDuration("duration"))
        .isEqualTo(Duration.ofMillis(8));
  }

  @Test
  void defaultMethodsDelegate() {
    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("dog", "bark");
    expectedMap.put("cat", "meow");
    expectedMap.put("bear", "growl");

    Map<String, String> map = makeTestProps();
    ConfigProperties properties = DefaultConfigProperties.create(map, COMPONENT_LOADER);
    assertThat(properties.getBoolean("test.boolean", false)).isTrue();
    assertThat(properties.getString("test.string", "nah")).isEqualTo("str");
    assertThat(properties.getDouble("test.double", 65.535)).isEqualTo(5.4);
    assertThat(properties.getInt("test.int", 21)).isEqualTo(10);
    assertThat(properties.getLong("test.long", 123L)).isEqualTo(20L);
    assertThat(properties.getDuration("test.duration", Duration.ofDays(13)))
        .isEqualTo(Duration.ofSeconds(1));
    assertThat(properties.getList("test.list", emptyList())).containsExactly("cat", "dog", "bear");
    assertThat(properties.getMap("test.map", emptyMap())).containsAllEntriesOf(expectedMap);
  }

  @Test
  void defaultMethodsFallBack() {
    ConfigProperties properties = DefaultConfigProperties.create(emptyMap(), COMPONENT_LOADER);
    assertThat(properties.getBoolean("foo", true)).isTrue();
    assertThat(properties.getString("foo", "bar")).isEqualTo("bar");
    assertThat(properties.getDouble("foo", 65.535)).isEqualTo(65.535);
    assertThat(properties.getInt("foo", 21)).isEqualTo(21);
    assertThat(properties.getLong("foo", 123L)).isEqualTo(123L);
    assertThat(properties.getDuration("foo", Duration.ofDays(13))).isEqualTo(Duration.ofDays(13));
  }

  @Test
  void defaultCollectionTypes() {
    ConfigProperties properties = DefaultConfigProperties.create(emptyMap(), COMPONENT_LOADER);
    assertThat(properties.getList("foo", Arrays.asList("1", "2", "3")))
        .containsExactly("1", "2", "3");
    assertThat(properties.getList("foo")).isEmpty();
    Map<String, String> defaultMap = new HashMap<>();
    defaultMap.put("one", "1");
    defaultMap.put("two", "2");
    assertThat(properties.getMap("foo", defaultMap))
        .containsExactly(entry("one", "1"), entry("two", "2"));
    assertThat(properties.getMap("foo")).isEmpty();
  }

  private static Map<String, String> makeTestProps() {
    Map<String, String> properties = new HashMap<>();
    properties.put("test.string", "str");
    properties.put("test.int", "10");
    properties.put("test.long", "20");
    properties.put("test.double", "5.4");
    properties.put("test.boolean", "true");
    properties.put("test.list", "cat,dog,bear");
    properties.put("test.map", "cat=meow,dog=bark,bear=growl,bird=");
    properties.put("test.duration", "1s");
    return properties;
  }
}
