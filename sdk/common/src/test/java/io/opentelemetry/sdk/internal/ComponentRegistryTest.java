/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

  private static final String NAME = "test_name";
  private static final String VERSION = "version";
  private static final String SCHEMA_URL = "http://schema.com";
  private static final Attributes ATTRIBUTES = Attributes.builder().put("k1", "v1").build();
  private final ComponentRegistry<TestComponent> registry =
      new ComponentRegistry<>(TestComponent::new);

  @Test
  void get_NameAndAttributesMustNotByNull() {
    assertThatThrownBy(() -> registry.get(null, VERSION, SCHEMA_URL, Attributes.empty()))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");

    assertThatThrownBy(() -> registry.get(NAME, VERSION, SCHEMA_URL, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void get_VersionAndSchemaAreNullable() {
    TestComponent testComponent = registry.get(NAME, null, null, Attributes.empty());
    assertThat(testComponent).isNotNull();
    assertThat(testComponent.instrumentationScopeInfo.getName()).isEqualTo(NAME);
    assertThat(testComponent.instrumentationScopeInfo.getVersion()).isNull();
    assertThat(testComponent.instrumentationScopeInfo.getSchemaUrl()).isNull();
  }

  @Test
  void get_SameInstance() {
    assertThat(registry.get(NAME, null, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, null, Attributes.empty()));
    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, null, Attributes.empty()));
    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()));
    assertThat(registry.get(NAME, null, null, ATTRIBUTES))
        .isSameAs(registry.get(NAME, null, null, ATTRIBUTES));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isSameAs(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES));
  }

  @Test
  void get_DifferentInstance() {
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME + "_1", VERSION, SCHEMA_URL, ATTRIBUTES));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION + "_1", SCHEMA_URL, ATTRIBUTES));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION, SCHEMA_URL + "_1", ATTRIBUTES));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(
            registry.get(NAME, VERSION, SCHEMA_URL, Attributes.builder().put("k1", "v2").build()));
    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));
    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));
    assertThat(registry.get(NAME, null, null, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));
  }

  private static final class TestComponent {
    private final InstrumentationScopeInfo instrumentationScopeInfo;

    private TestComponent(InstrumentationScopeInfo instrumentationScopeInfo) {
      this.instrumentationScopeInfo = instrumentationScopeInfo;
    }
  }
}
