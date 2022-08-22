/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class InstrumentationScopeInfoTest {

  @Test
  void create_InvalidArguments() {
    assertThatThrownBy(() -> InstrumentationScopeInfo.builder(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void create_Valid() {
    assertThatCode(() -> InstrumentationScopeInfo.builder("name").build())
        .doesNotThrowAnyException();
    assertThatCode(() -> InstrumentationScopeInfo.builder("name").setVersion(null).build())
        .doesNotThrowAnyException();
    assertThatCode(() -> InstrumentationScopeInfo.builder("name").setSchemaUrl(null).build())
        .doesNotThrowAnyException();
    assertThatCode(
            () ->
                InstrumentationScopeInfo.builder("name")
                    .setAttributes(Attributes.builder().put("key", "value").build())
                    .build())
        .doesNotThrowAnyException();
  }

  @Test
  void emptyScopeInfo() {
    assertThat(InstrumentationScopeInfo.empty().getName()).isEmpty();
    assertThat(InstrumentationScopeInfo.empty().getVersion()).isNull();
  }
}
