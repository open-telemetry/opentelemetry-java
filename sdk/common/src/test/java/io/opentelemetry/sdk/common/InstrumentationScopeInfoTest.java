/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import static org.assertj.core.api.Assertions.assertThat;
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
    InstrumentationScopeInfo scope = InstrumentationScopeInfo.builder("name").build();
    assertThat(scope.getName()).isEqualTo("name");
    assertThat(scope.getVersion()).isNull();
    assertThat(scope.getSchemaUrl()).isNull();
    assertThat(scope.getAttributes()).isEqualTo(Attributes.empty());

    scope =
        InstrumentationScopeInfo.builder("name")
            .setVersion("version")
            .setSchemaUrl("schemaUrl")
            .setAttributes(Attributes.builder().put("key", "value").build())
            .build();
    assertThat(scope.getName()).isEqualTo("name");
    assertThat(scope.getVersion()).isEqualTo("version");
    assertThat(scope.getSchemaUrl()).isEqualTo("schemaUrl");
    assertThat(scope.getAttributes()).isEqualTo(Attributes.builder().put("key", "value").build());
  }

  @Test
  @SuppressWarnings("deprecation") // Testing deprecated code
  void create_AllArgs() {
    assertThat(InstrumentationScopeInfo.create("name", "version", "schemaUrl"))
        .isEqualTo(
            InstrumentationScopeInfo.builder("name")
                .setVersion("version")
                .setSchemaUrl("schemaUrl")
                .build());
    assertThat(InstrumentationScopeInfo.create("name", null, null))
        .isEqualTo(InstrumentationScopeInfo.builder("name").build());
  }

  @Test
  void emptyScopeInfo() {
    assertThat(InstrumentationScopeInfo.empty().getName()).isEmpty();
    assertThat(InstrumentationScopeInfo.empty().getVersion()).isNull();
  }
}
