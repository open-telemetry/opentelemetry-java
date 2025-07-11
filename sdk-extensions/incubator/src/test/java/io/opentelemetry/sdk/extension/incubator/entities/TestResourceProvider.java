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

class TestResourceProvider {
  @Test
  void defaults_includeServiceAndSdk() {
    SdkResourceProvider provider = SdkResourceProvider.builder().includeDefaults(true).build();

    assertThat(provider.getSdkResource().getAttributes())
        .containsKey("service.name")
        .containsKey("service.instance.id")
        .containsKey("telemetry.sdk.language")
        .containsKey("telemetry.sdk.name")
        .containsKey("telemetry.sdk.version");
    assertThat(provider.getSdkResource().getSchemaUrl())
        .isEqualTo("https://opentelemetry.io/schemas/1.34.0");

    assertThat(EntityUtil.getEntities(provider.getSdkResource()))
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e).hasType("service"), e -> assertThat(e).hasType("telemetry.sdk"));
  }

  @Test
  void Resource_updatesDescription() {
    SdkResourceProvider provider = SdkResourceProvider.builder().includeDefaults(false).build();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getSdkResource().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("one.desc");
  }

  @Test
  void Resource_ignoresNewIds() {
    SdkResourceProvider provider = SdkResourceProvider.builder().includeDefaults(false).build();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 2).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getSdkResource().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void Resource_ignoresNewSchemaUrl() {
    SdkResourceProvider provider = SdkResourceProvider.builder().includeDefaults(false).build();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(provider.getSdkResource().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void resource_addsNewEntity() {
    SdkResourceProvider provider = SdkResourceProvider.builder().includeDefaults(false).build();

    provider
        .getResource()
        .attachEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .getResource()
        .attachEntity("two")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("two.id", 2).build())
        .emit();

    assertThat(provider.getSdkResource().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("two.id");
  }
}
