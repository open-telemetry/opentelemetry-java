/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // Testing deprecated code
class InstrumentationScopeUtilTest {

  @Test
  void toInstrumentationLibraryInfo() {
    assertThat(
            InstrumentationScopeUtil.toInstrumentationLibraryInfo(
                InstrumentationScopeInfo.builder("name").build()))
        .isEqualTo(InstrumentationLibraryInfo.create("name", null, null));
    assertThat(
            InstrumentationScopeUtil.toInstrumentationLibraryInfo(
                InstrumentationScopeInfo.builder("name")
                    .setVersion("version")
                    .setSchemaUrl("schemaUrl")
                    .setAttributes(Attributes.builder().put("key", "value").build())
                    .build()))
        .isEqualTo(InstrumentationLibraryInfo.create("name", "version", "schemaUrl"));
  }

  @Test
  void toInstrumentationScopeInfo() {
    assertThat(
            InstrumentationScopeUtil.toInstrumentationScopeInfo(
                InstrumentationLibraryInfo.create("name", null, null)))
        .isEqualTo(InstrumentationScopeInfo.builder("name").build());
    assertThat(
            InstrumentationScopeUtil.toInstrumentationScopeInfo(
                InstrumentationLibraryInfo.create("name", "version", "schemaUrl")))
        .isEqualTo(
            InstrumentationScopeInfo.builder("name")
                .setVersion("version")
                .setSchemaUrl("schemaUrl")
                .build());
  }
}
