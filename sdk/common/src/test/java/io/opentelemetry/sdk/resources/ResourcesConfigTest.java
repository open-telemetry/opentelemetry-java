/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

class ResourcesConfigTest {

  @Test
  void defaultResourcesConfig() {
    assertThat(ResourcesConfig.getDefault().getDisabledResourceProviders())
        .isEqualTo(ImmutableSet.of());
  }

  @Test
  void updateResourcesConfig_NullDisabledResourceProviders() {
    assertThrows(
        NullPointerException.class,
        () -> ResourcesConfig.getDefault().toBuilder().setDisabledResourceProviders(null).build());
  }

  @Test
  void updateResourcesConfig_SystemProperties() {
    System.setProperty(
        "otel.java.disabled.resource_providers",
        "com.package.provider.ToDisable1, com.package.provider.ToDisable2");
    ResourcesConfig resourcesConfig =
        ResourcesConfig.newBuilder().readSystemProperties().readEnvironmentVariables().build();
    assertThat(resourcesConfig.getDisabledResourceProviders())
        .isEqualTo(
            ImmutableSet.of("com.package.provider.ToDisable1", "com.package.provider.ToDisable2"));
  }

  @Test
  void updateResourcesConfig_EmptyDisabledResourceProviders() {
    System.setProperty("otel.java.disabled.resource_providers", "");
    ResourcesConfig resourcesConfig =
        ResourcesConfig.newBuilder().readSystemProperties().readEnvironmentVariables().build();
    assertThat(resourcesConfig.getDisabledResourceProviders()).isEqualTo(ImmutableSet.of());
  }

  @Test
  void updateResourcesConfig_All() {
    ResourcesConfig resourcesConfig =
        ResourcesConfig.newBuilder()
            .setDisabledResourceProviders(ImmutableSet.of("com.package.provider.ToDisable"))
            .build();
    assertThat(resourcesConfig.getDisabledResourceProviders())
        .isEqualTo(ImmutableSet.of("com.package.provider.ToDisable"));
  }
}
