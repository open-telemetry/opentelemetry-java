/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

  private static final String INSTRUMENTATION_NAME = "test_name";
  private static final String INSTRUMENTATION_VERSION = "version";
  private final ComponentRegistry<TestComponent> registry =
      new ComponentRegistry<>(TestComponent::new);

  @Test
  void scopeName_MustNotBeNull() {
    assertThatThrownBy(() -> registry.get(null, "version"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void scopeVersion_AllowsNull() {
    TestComponent testComponent = registry.get(INSTRUMENTATION_NAME, null);
    assertThat(testComponent).isNotNull();
    assertThat(testComponent.instrumentationScopeInfo.getName()).isEqualTo(INSTRUMENTATION_NAME);
    assertThat(testComponent.instrumentationScopeInfo.getVersion()).isNull();
  }

  @Test
  void getSameInstanceForSameName_WithoutVersion() {
    assertThat(registry.get(INSTRUMENTATION_NAME)).isSameAs(registry.get(INSTRUMENTATION_NAME));
    assertThat(registry.get(INSTRUMENTATION_NAME))
        .isSameAs(registry.get(INSTRUMENTATION_NAME, null));
  }

  @Test
  void getSameInstanceForSameName_WithVersion() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isSameAs(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION));
  }

  @Test
  void getDifferentInstancesForDifferentNames() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isNotSameAs(registry.get(INSTRUMENTATION_NAME + "_2", INSTRUMENTATION_VERSION));
  }

  @Test
  void getDifferentInstancesForDifferentVersions() {
    assertThat(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION))
        .isNotSameAs(registry.get(INSTRUMENTATION_NAME, INSTRUMENTATION_VERSION + "_1"));
  }

  private static final class TestComponent {
    private final InstrumentationScopeInfo instrumentationScopeInfo;

    private TestComponent(InstrumentationScopeInfo instrumentationScopeInfo) {
      this.instrumentationScopeInfo = instrumentationScopeInfo;
    }
  }
}
