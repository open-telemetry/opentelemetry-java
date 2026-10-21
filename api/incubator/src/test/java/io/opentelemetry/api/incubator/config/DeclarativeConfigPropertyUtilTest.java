/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.YamlDeclarativeConfigProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DeclarativeConfigPropertyUtilTest {

  @RegisterExtension
  LogCapturer logCapturer =
      LogCapturer.create().captureForType(YamlDeclarativeConfigProperties.class);

  @Test
  void toMap_doesNotLogTypeWarnings() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("string_key", "value");
    properties.put("bool_key", true);
    properties.put("int_key", 1);
    properties.put("double_key", 0.25);
    properties.put("map_key", Collections.singletonMap("ratio", 0.25));
    properties.put("string_list_key", Arrays.asList("a", "b"));
    properties.put(
        "structured_list_key", Collections.singletonList(Collections.singletonMap("k", false)));
    DeclarativeConfigProperties config =
        YamlDeclarativeConfigProperties.create(
            properties, ComponentLoader.forClassLoader(getClass().getClassLoader()));

    Map<String, Object> result = DeclarativeConfigProperties.toMap(config);

    assertThat(result)
        .containsExactlyInAnyOrderEntriesOf(
            ImmutableMap.<String, Object>builder()
                .put("string_key", "value")
                .put("bool_key", true)
                .put("int_key", 1L)
                .put("double_key", 0.25)
                .put("map_key", ImmutableMap.of("ratio", 0.25))
                .put("string_list_key", Arrays.asList("a", "b"))
                .put("structured_list_key", Collections.singletonList(ImmutableMap.of("k", false)))
                .build());
    assertThat(logCapturer.getEvents()).isEmpty();
  }
}
