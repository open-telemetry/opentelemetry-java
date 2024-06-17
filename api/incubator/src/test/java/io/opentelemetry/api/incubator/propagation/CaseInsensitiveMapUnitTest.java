/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** only unit test CaseInsensitiveMapUnitTest. */
public class CaseInsensitiveMapUnitTest {

  /** test Constructor method. */
  @Test
  void createByConstructor() {
    Map<String, String> map = new HashMap<>();
    map.put("KeY1", "test");
    map.put("KeY2", "test2");

    CaseInsensitiveMap caseInsensitiveMap = new CaseInsensitiveMap(map);

    for (Map.Entry<String, String> caseEntry : caseInsensitiveMap.entrySet()) {
      String k = caseEntry.getKey();
      String v = caseEntry.getValue();
      boolean result = false;
      for (Map.Entry<String, String> mapEntry : map.entrySet()) {
        String k1 = mapEntry.getKey();
        String v1 = mapEntry.getValue();
        boolean equals = caseInsensitiveMap.getKeyLowerCase(k1).equals(k);
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

  /** test put and get method. */
  @Test
  void getMethodUnitTest() {
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
