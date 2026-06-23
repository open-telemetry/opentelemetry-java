/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
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
    // Keys with the same hashcode always share a bucket (independent of capacity), mirroring
    // collection removing a stale series while iterating the others.
    PooledHashMap<SameBucketKey, Integer> collidingMap = new PooledHashMap<>();
    SameBucketKey first = new SameBucketKey("first");
    SameBucketKey second = new SameBucketKey("second");
    collidingMap.put(first, 1);
    collidingMap.put(second, 2);

    Map<SameBucketKey, Integer> visited = new HashMap<>();
    collidingMap.forEach(
        (key, value) -> {
          visited.put(key, value);
          if (visited.size() == 1) {
            collidingMap.remove(key); // drop the first one we see
          }
        });

    assertThat(visited).containsOnlyKeys(first, second).containsValues(1, 2);
    assertThat(collidingMap.size()).isEqualTo(1);
  }

  /** Key whose hashcode is constant, so all instances fall in the same bucket. */
  private static final class SameBucketKey {
    private final String name;

    SameBucketKey(String name) {
      this.name = name;
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      return o instanceof SameBucketKey && name.equals(((SameBucketKey) o).name);
    }
  }
}
