/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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

    Map<String, String> expectMap = new HashMap<>();
    expectMap.put("key1", "test");
    expectMap.put("key2", "test2");

    assertThat(caseInsensitiveMap).isEqualTo(expectMap);
  }

  @Test
  void createByConstructorWithNullMap() {
    assertThatCode(() -> new CaseInsensitiveMap(null)).doesNotThrowAnyException();
  }

  @Test
  void putMethodTest() {

    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(null);

    assertThat(caseInsensitiveMap).isEmpty();

    String key1 = "KEY1";
    String value1 = "test1";
    caseInsensitiveMap.put(key1, value1);

    String key2 = "KEY2";
    String value2 = "test2";
    caseInsensitiveMap.put(key2, value2);

    Map<String, String> expectMap = new HashMap<>();
    expectMap.put("key1", "test1");
    expectMap.put("key2", "test2");

    // test put
    assertThat(caseInsensitiveMap).isEqualTo(expectMap);
  }

  @Test
  void getMethodTest() {

    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(null);

    assertThat(caseInsensitiveMap).isEmpty();

    String key1 = "key1";
    String value1 = "test1";
    caseInsensitiveMap.put(key1, value1);

    String key2 = "key2";
    String value2 = "test2";
    caseInsensitiveMap.put(key2, value2);

    Map<String, String> expectMap = new HashMap<>();
    expectMap.put("KEY1", "test1");
    expectMap.put("KEY2", "test2");

    // test get
    expectMap.forEach((k, v) -> assertThat(v).isEqualTo(caseInsensitiveMap.get(k)));
  }
}
