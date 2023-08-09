package io.opentelemetry.sdk.metrics.internal.state;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PooledHashMapTest {

    private PooledHashMap<String, Integer> map;

    @BeforeEach
    public void setup() {
        map = new PooledHashMap<>();
    }

    @Test
    public void putAndGetTest() {
        map.put("One", 1);
        Assertions.assertThat(map.get("One")).isEqualTo(1);
    }

    @Test
    public void removeTest() {
        map.put("One", 1);
        map.remove("One");
        Assertions.assertThat(map.get("One")).isNull();
    }

    @Test
    public void sizeTest() {
        map.put("One", 1);
        map.put("Two", 2);
        Assertions.assertThat(map.size()).isEqualTo(2);
    }

    @Test
    public void isEmptyTest() {
        Assertions.assertThat(map.isEmpty()).isTrue();
        map.put("One", 1);
        Assertions.assertThat(map.isEmpty()).isFalse();
    }

    @Test
    public void containsKeyTest() {
        map.put("One", 1);
        Assertions.assertThat(map.containsKey("One")).isTrue();
        Assertions.assertThat(map.containsKey("Two")).isFalse();
    }

    @Test
    public void clearTest() {
        map.put("One", 1);
        map.put("Two", 2);
        map.clear();
        Assertions.assertThat(map.isEmpty()).isTrue();
    }

    @Test
    public void forEachTest() {
        map.put("One", 1);
        map.put("Two", 2);

        final Map<String, Integer> actualMap = new HashMap<>();
        map.forEach(actualMap::put);

        Assertions.assertThat(actualMap)
                .containsOnlyKeys("One", "Two")
                .containsValues(1, 2);
    }
}
