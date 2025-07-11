/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import org.junit.jupiter.api.Test;

class TestEntityProvider {
  @Test
  void defaults_includeServiceAndSdk() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(true).build();

    assertThat(provider.getResource().getAttributes())
        .containsKey("service.name")
        .containsKey("service.instance.id")
        .containsKey("telemetry.sdk.language")
        .containsKey("telemetry.sdk.name")
        .containsKey("telemetry.sdk.version");
    assertThat(provider.getResource().getSchemaUrl())
        .isEqualTo("https://opentelemetry.io/schemas/1.34.0");

    assertThat(EntityUtil.getEntities(provider.getResource()))
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e).hasType("service"), e -> assertThat(e).hasType("telemetry.sdk"));
  }

  @Test
  void resource_updatesDescription() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getResource().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("one.desc");
  }

  @Test
  void resource_ignoresNewIds() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 2).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getResource().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void resource_ignoresNewSchemaUrl() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getResource().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void resource_addsNewEntity() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("two")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("two.id", 2).build())
        .emit();

    assertThat(provider.getResource().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("two.id");
  }
}
