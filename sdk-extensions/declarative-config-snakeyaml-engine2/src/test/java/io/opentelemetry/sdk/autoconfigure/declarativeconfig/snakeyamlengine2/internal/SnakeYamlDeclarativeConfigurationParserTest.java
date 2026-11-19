/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.snakeyamlengine2.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.config.DeclarativeConfigException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

class SnakeYamlDeclarativeConfigurationParserTest {

  @Test
  void wrapsSnakeYamlException() {
    ByteArrayInputStream input =
        new ByteArrayInputStream("key: [unclosed".getBytes(StandardCharsets.UTF_8));
    assertThatThrownBy(() ->
            new SnakeYamlDeclarativeConfigurationParser()
                .parse(input, Collections.emptyMap(), Collections.emptyMap()))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasCauseInstanceOf(YamlEngineException.class);
  }
}
