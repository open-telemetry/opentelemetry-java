/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

  private static final String NAME = "test_name";
  private static final String VERSION = "version";
  private static final String SCHEMA_URL = "http://schema.com";
  private static final Attributes ATTRIBUTES = Attributes.builder().put("k1", "v1").build();
  private final ComponentRegistry<TestComponent> registry =
      new ComponentRegistry<>(unused -> new TestComponent());

  @Test
  void get_SameInstance() {
    assertThat(registry.get(InstrumentationScopeInfo.builder(NAME).build()))
        .isSameAs(registry.get(InstrumentationScopeInfo.builder(NAME).build()));
    assertThat(registry.get(InstrumentationScopeInfo.builder(NAME).setVersion(VERSION).build()))
        .isSameAs(registry.get(InstrumentationScopeInfo.builder(NAME).setVersion(VERSION).build()))
        .isSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setAttributes(ATTRIBUTES)
                    .build()));
    assertThat(
            registry.get(InstrumentationScopeInfo.builder(NAME).setSchemaUrl(SCHEMA_URL).build()))
        .isSameAs(
            registry.get(InstrumentationScopeInfo.builder(NAME).setSchemaUrl(SCHEMA_URL).build()));
    assertThat(
            registry.get(InstrumentationScopeInfo.builder(NAME).setAttributes(ATTRIBUTES).build()))
        .isSameAs(registry.get(InstrumentationScopeInfo.builder(NAME).build()))
        .isSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setAttributes(Attributes.builder().put("k1", "v2").build())
                    .build()));
    assertThat(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL)
                    .setAttributes(ATTRIBUTES)
                    .build()))
        .isSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL)
                    .setAttributes(ATTRIBUTES)
                    .build()))
        .isSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL)
                    .setAttributes(Attributes.builder().put("k1", "v2").build())
                    .build()))
        .isSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL)
                    .build()));
  }

  @Test
  void get_DifferentInstance() {
    InstrumentationScopeInfo allFields =
        InstrumentationScopeInfo.builder(NAME)
            .setVersion(VERSION)
            .setSchemaUrl(SCHEMA_URL)
            .setAttributes(ATTRIBUTES)
            .build();

    assertThat(registry.get(allFields))
        .isNotSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME + "_1")
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL)
                    .setAttributes(ATTRIBUTES)
                    .build()));
    assertThat(registry.get(allFields))
        .isNotSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION + "_1")
                    .setSchemaUrl(SCHEMA_URL)
                    .setAttributes(ATTRIBUTES)
                    .build()));
    assertThat(registry.get(allFields))
        .isNotSameAs(
            registry.get(
                InstrumentationScopeInfo.builder(NAME)
                    .setVersion(VERSION)
                    .setSchemaUrl(SCHEMA_URL + "_1")
                    .setAttributes(ATTRIBUTES)
                    .build()));
    assertThat(registry.get(InstrumentationScopeInfo.builder(NAME).setVersion(VERSION).build()))
        .isNotSameAs(registry.get(InstrumentationScopeInfo.builder(NAME).build()));
    assertThat(
            registry.get(InstrumentationScopeInfo.builder(NAME).setSchemaUrl(SCHEMA_URL).build()))
        .isNotSameAs(registry.get(InstrumentationScopeInfo.builder(NAME).build()));
  }

  private static final class TestComponent {}
}
