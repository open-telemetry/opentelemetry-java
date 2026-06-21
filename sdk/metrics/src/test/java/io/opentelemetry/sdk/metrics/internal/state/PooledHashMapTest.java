/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PooledHashMapTest {

  private PooledHashMap<String, Integer> map;

  @BeforeEach
  void setup() {
    map = new PooledHashMap<>();
  }

  @Test
  void putAndGetTest() {
    map.put("One", 1);
    assertThat(map.get("One")).isEqualTo(1);
  }

  @Test
  void removeTest() {
    map.put("One", 1);
    map.remove("One");
    assertThat(map.get("One")).isNull();
  }

  @Test
  void sizeTest() {
    map.put("One", 1);
    map.put("Two", 2);
    assertThat(map.size()).isEqualTo(2);
  }

  @Test
  void isEmptyTest() {
    assertThat(map.isEmpty()).isTrue();
    map.put("One", 1);
    assertThat(map.isEmpty()).isFalse();
  }

  @Test
  void containsKeyTest() {
    map.put("One", 1);
    assertThat(map.containsKey("One")).isTrue();
    assertThat(map.containsKey("Two")).isFalse();
  }

  @Test
  void clearTest() {
    map.put("One", 1);
    map.put("Two", 2);
    map.clear();
    assertThat(map.isEmpty()).isTrue();
  }

  @Test
  void forEachTest() {
    map.put("One", 1);
    map.put("Two", 2);

    Map<String, Integer> actualMap = new HashMap<>();
    map.forEach(actualMap::put);

    assertThat(actualMap).containsOnlyKeys("One", "Two").containsValues(1, 2);
  }

  @Test
  void forEachAllowsRemovalOfCurrentEntry() {
    // 1 and 17 land in the same bucket, like collection removing a stale series mid-iteration.
    PooledHashMap<Integer, Integer> collidingMap = new PooledHashMap<>();
    collidingMap.put(1, 1);
    collidingMap.put(17, 17);

    Map<Integer, Integer> visited = new HashMap<>();
    collidingMap.forEach(
        (key, value) -> {
          visited.put(key, value);
          if (visited.size() == 1) {
            collidingMap.remove(key); // drop the first one we see
          }
        });

    assertThat(visited).containsOnlyKeys(1, 17).containsValues(1, 17);
    assertThat(collidingMap.size()).isEqualTo(1);
  }
}
