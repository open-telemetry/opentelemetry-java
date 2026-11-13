/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.jackson2.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.config.DeclarativeConfigException;
import org.junit.jupiter.api.Test;

class JacksonDeclarativeConfigurationMapperTest {

  private final JacksonDeclarativeConfigurationMapper mapper =
      new JacksonDeclarativeConfigurationMapper();

  @Test
  void convertWrapsJacksonException() {
    assertThatThrownBy(() -> mapper.convert("not-an-integer", Integer.class))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toMapWrapsJacksonException() {
    assertThatThrownBy(() -> mapper.toMap(42))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class);
  }
}
