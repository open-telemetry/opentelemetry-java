/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class DeclarativeConfigPropertiesShimTest {

  @Test
  void empty() {
    io.opentelemetry.api.config.DeclarativeConfigProperties empty =
        DeclarativeConfigProperties.empty();
    assertThat(empty).isNotNull();
    assertThat(empty.getPropertyKeys()).isEmpty();
  }

  @Test
  void toMap() {
    io.opentelemetry.api.config.DeclarativeConfigProperties empty =
        DeclarativeConfigProperties.empty();
    Map<String, Object> map = DeclarativeConfigProperties.toMap(empty);
    assertThat(map).isNotNull().isEmpty();
  }
}
