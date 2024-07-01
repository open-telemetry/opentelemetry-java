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

    for (Map.Entry<String, String> mapEntry : map.entrySet()) {
      String k1 = mapEntry.getKey();
      String v1 = mapEntry.getValue();
      assertThat(v1).isEqualTo(caseInsensitiveMap.get(k1));
    }
  }

  @Test
  void createByConstructorWithNullMap() {
    assertThatCode(() -> new CaseInsensitiveMap(null)).doesNotThrowAnyException();
  }

  @Test
  void getAndPut() {
    Map<String, String> map = new HashMap<>();
    String key1 = "KEY1";
    String value1 = "test";
    map.put(key1, value1);
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(map);

    for (Map.Entry<String, String> mapEntry : map.entrySet()) {
      String k1 = mapEntry.getKey();
      String v1 = mapEntry.getValue();
      assertThat(v1).isEqualTo(caseInsensitiveMap.get(k1));
    }

    String key2 = "KEY2";
    String value2 = "test2";
    map.put(key2, value2);

    caseInsensitiveMap.put(key2, value2);

    for (Map.Entry<String, String> mapEntry : map.entrySet()) {
      String k1 = mapEntry.getKey();
      String v1 = mapEntry.getValue();
      assertThat(v1).isEqualTo(caseInsensitiveMap.get(k1));
    }
  }
}
