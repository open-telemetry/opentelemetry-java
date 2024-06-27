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

    for (Map.Entry<String, String> caseEntry : caseInsensitiveMap.entrySet()) {
      String k = caseEntry.getKey();
      String v = caseEntry.getValue();
      boolean result = false;
      for (Map.Entry<String, String> mapEntry : map.entrySet()) {
        String k1 = mapEntry.getKey();
        String v1 = mapEntry.getValue();

        String keyLowerCase = caseInsensitiveMap.getKeyLowerCase(k1);

        boolean equals = keyLowerCase.equals(k);
        if (equals) {
          if (v1.equals(v)) {
            result = true;
            break;
          }
        }
      }
      assertThat(result).isTrue();
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

    String key2 = "KEY2";
    String value2 = "test2";
    map.put(key2, value2);

    caseInsensitiveMap.put(key2, value2);

    Map<String, String> lowCaseKeyMap = new HashMap<>();

    lowCaseKeyMap.put(caseInsensitiveMap.getKeyLowerCase(key1), value1);
    lowCaseKeyMap.put(caseInsensitiveMap.getKeyLowerCase(key2), value2);

    assertThat(lowCaseKeyMap).isEqualTo(caseInsensitiveMap);

    for (String s : map.keySet()) {
      // test get method
      assertThat(caseInsensitiveMap.get(s)).isEqualTo(map.get(s));
    }
  }
}
