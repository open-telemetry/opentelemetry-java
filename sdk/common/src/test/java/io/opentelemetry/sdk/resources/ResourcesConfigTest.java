/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
