/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CaseInsensitiveMapTest {

  @Test
  void createByConstructor() {
    Map<String, String> map = new HashMap<>();
    map.put("KeY1", "test");
    map.put("KeY2", "test2");

    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(map);

    Method getKeyLowerCaseMethod = null;
    try {
      getKeyLowerCaseMethod =
          CaseInsensitiveMap.class.getDeclaredMethod("getKeyLowerCase", String.class);
      getKeyLowerCaseMethod.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(" CaseInsensitiveMap class getKeyLowerCase method not found ", e);
    }

    for (Map.Entry<String, String> caseEntry : caseInsensitiveMap.entrySet()) {
      String k = caseEntry.getKey();
      String v = caseEntry.getValue();
      boolean result = false;
      for (Map.Entry<String, String> mapEntry : map.entrySet()) {
        String k1 = mapEntry.getKey();
        String v1 = mapEntry.getValue();

        String keyLowerCase;
        try {
          keyLowerCase = (String) getKeyLowerCaseMethod.invoke(caseInsensitiveMap, k1);
        } catch (Exception e) {
          throw new RuntimeException(
              " CaseInsensitiveMap class getKeyLowerCase method invoke fail ", e);
        }

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
    assertThatCode(
            () -> {
              new CaseInsensitiveMap(null);
            })
        .doesNotThrowAnyException();
  }

  @Test
  void getAndPut() {
    Map<String, String> map = new HashMap<>();
    map.put("KeY1", "test");
    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(map);

    map.put("KeY2", "test2");

    caseInsensitiveMap.put("KeY2", "test2");

    for (String s : map.keySet()) {
      assertThat(caseInsensitiveMap.get(s)).isEqualTo(map.get(s));
    }
  }
}
