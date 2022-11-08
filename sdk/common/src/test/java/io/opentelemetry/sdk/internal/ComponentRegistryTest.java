/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
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
    assertThat(registry.get(NAME, null, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, null, Attributes.builder().put("k1", "v2").build()));

    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, null, Attributes.builder().put("k1", "v2").build()));
    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isSameAs(
            registry.get(NAME, null, SCHEMA_URL, Attributes.builder().put("k1", "v2").build()));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, SCHEMA_URL, Attributes.empty()))
        .isSameAs(
            registry.get(NAME, VERSION, SCHEMA_URL, Attributes.builder().put("k1", "v2").build()));
  }

  @Test
  void get_DifferentInstance() {
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME + "_1", VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION + "_1", SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION, SCHEMA_URL + "_1", ATTRIBUTES));

    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));

    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));
  }

  private static final class TestComponent {}
}
