/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.ComponentLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapBackedDeclarativeConfigPropertiesTest {

  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(
          MapBackedDeclarativeConfigPropertiesTest.class.getClassLoader());

  @Test
  void getString() {
    DeclarativeConfigProperties config = fromMap(mapOf("key", "value", "notString", 42));

    assertThat(config.getString("key")).isEqualTo("value");
    assertThat(config.getString("missing")).isNull();
    assertThat(config.getString("notString")).isNull();
  }

  @Test
  void getBoolean() {
    DeclarativeConfigProperties config = fromMap(mapOf("key", true, "notBoolean", "true"));

    assertThat(config.getBoolean("key")).isTrue();
    assertThat(config.getBoolean("missing")).isNull();
    assertThat(config.getBoolean("notBoolean")).isNull();
  }

  @Test
  void getInt() {
    DeclarativeConfigProperties config = fromMap(mapOf("intVal", 42, "longVal", 100L, "str", "x"));

    assertThat(config.getInt("intVal")).isEqualTo(42);
    assertThat(config.getInt("longVal")).isEqualTo(100);
    assertThat(config.getInt("missing")).isNull();
    assertThat(config.getInt("str")).isNull();
  }

  @Test
  void getLong() {
    DeclarativeConfigProperties config = fromMap(mapOf("longVal", 100L, "intVal", 42, "str", "x"));

    assertThat(config.getLong("longVal")).isEqualTo(100L);
    assertThat(config.getLong("intVal")).isEqualTo(42L);
    assertThat(config.getLong("missing")).isNull();
    assertThat(config.getLong("str")).isNull();
  }

  @Test
  void getDouble() {
    DeclarativeConfigProperties config =
        fromMap(mapOf("doubleVal", 3.14, "intVal", 42, "str", "x"));

    assertThat(config.getDouble("doubleVal")).isEqualTo(3.14);
    assertThat(config.getDouble("intVal")).isEqualTo(42.0);
    assertThat(config.getDouble("missing")).isNull();
    assertThat(config.getDouble("str")).isNull();
  }

  @Test
  void getScalarList() {
    DeclarativeConfigProperties config =
        fromMap(mapOf("strings", Arrays.asList("a", "b"), "mixed", Arrays.asList("a", 1)));

    assertThat(config.getScalarList("strings", String.class)).containsExactly("a", "b");
    assertThat(config.getScalarList("mixed", String.class)).isNull();
    assertThat(config.getScalarList("missing", String.class)).isNull();
  }

  @Test
  void getScalarList_nonListReturnsNull() {
    DeclarativeConfigProperties config = fromMap(mapOf("notList", "value"));

    assertThat(config.getScalarList("notList", String.class)).isNull();
  }

  @Test
  void getStructured() {
    Map<String, Object> child = mapOf("nested", "value");
    DeclarativeConfigProperties config = fromMap(mapOf("child", child, "notMap", "scalar"));

    DeclarativeConfigProperties structured = config.getStructured("child");
    assertThat(structured).isNotNull();
    assertThat(structured.getString("nested")).isEqualTo("value");
    assertThat(config.getStructured("missing")).isNull();
    assertThat(config.getStructured("notMap")).isNull();
  }

  @Test
  void getStructuredList() {
    List<Map<String, Object>> items =
        Arrays.asList(mapOf("name", "first"), mapOf("name", "second"));
    DeclarativeConfigProperties config =
        fromMap(mapOf("items", items, "badItems", Arrays.asList("notAMap")));

    List<DeclarativeConfigProperties> result = config.getStructuredList("items");
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getString("name")).isEqualTo("first");
    assertThat(result.get(1).getString("name")).isEqualTo("second");
    assertThat(config.getStructuredList("missing")).isNull();
    assertThat(config.getStructuredList("badItems")).isNull();
  }

  @Test
  void getStructuredList_nonListReturnsNull() {
    DeclarativeConfigProperties config = fromMap(mapOf("notList", "value"));

    assertThat(config.getStructuredList("notList")).isNull();
  }

  @Test
  void getPropertyKeys() {
    DeclarativeConfigProperties config = fromMap(mapOf("a", 1, "b", 2));

    assertThat(config.getPropertyKeys()).containsExactlyInAnyOrder("a", "b");
  }

  @Test
  void getPropertyKeys_empty() {
    DeclarativeConfigProperties config = fromMap(Collections.emptyMap());

    assertThat(config.getPropertyKeys()).isEmpty();
  }

  @Test
  void getComponentLoader() {
    DeclarativeConfigProperties config = fromMap(Collections.emptyMap());

    assertThat(config.getComponentLoader()).isSameAs(COMPONENT_LOADER);
  }

  @Test
  void get_defaultMethod() {
    Map<String, Object> child = mapOf("nested", "value");
    DeclarativeConfigProperties config = fromMap(mapOf("child", child));

    assertThat(config.get("child").getString("nested")).isEqualTo("value");
    assertThat(config.get("missing").getPropertyKeys()).isEmpty();
  }

  private static DeclarativeConfigProperties fromMap(Map<String, Object> map) {
    return DeclarativeConfigProperties.fromMap(map, COMPONENT_LOADER);
  }

  private static Map<String, Object> mapOf(Object... entries) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (int i = 0; i < entries.length; i += 2) {
      result.put((String) entries[i], entries[i + 1]);
    }
    return result;
  }
}
