/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CaseInsensitiveMapTest {

  @Test
  void createByConstructor() {
    Map<String, String> map = new HashMap<>();
    map.put("Key1", "test");
    map.put("Key2", "test2");

    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(map);

    Map<String, String> standardMap = new HashMap<>();
    standardMap.put("key1", "test");
    standardMap.put("key2", "test2");

    assertThat(caseInsensitiveMap).isEqualTo(standardMap);
  }

  @Test
  void putAll() {
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap();
    Map<String, String> standardMap = new HashMap<>();
    standardMap.put("key1", "test");
    standardMap.put("key2", "test2");
    caseInsensitiveMap.putAll(standardMap);
    assertThat(caseInsensitiveMap).isEqualTo(standardMap);
  }

  @Test
  void putIfAbsent() {
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap();
    caseInsensitiveMap.putIfAbsent("key1", "test");
    assertThat(caseInsensitiveMap.get("KEY1")).isEqualTo("test");
    caseInsensitiveMap.putIfAbsent("key1", "nope");
    assertThat(caseInsensitiveMap.get("KEY1")).isEqualTo("test");
  }

  @Test
  void createByConstructorWithNullMap() {
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(null);
    assertThat(caseInsensitiveMap).isEmpty();
  }

  @Test
  void caseInsensitivity() {
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(null);

    assertThat(caseInsensitiveMap).isEmpty();

    caseInsensitiveMap.put("KEY1", "test1");
    caseInsensitiveMap.put("KEY2", "test2");
    assertThat(caseInsensitiveMap.get("key1")).isEqualTo("test1");
    assertThat(caseInsensitiveMap.get("key2")).isEqualTo("test2");
    assertThat(caseInsensitiveMap.get("kEy2")).isEqualTo("test2");
    assertThat(caseInsensitiveMap.get("KEY2")).isEqualTo("test2");
  }
}
