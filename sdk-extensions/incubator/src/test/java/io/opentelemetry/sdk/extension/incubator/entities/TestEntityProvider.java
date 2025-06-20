/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.internal.EntityUtil;
import org.junit.jupiter.api.Test;

class TestEntityProvider {
  @Test
  void defaults_includeServiceAndSdk() {
    EntityProvider provider = EntityProvider.builder().includeDefaults(true).build();

    assertThat(provider.getResource().getAttributes())
        .containsKey("service.name")
        .containsKey("service.instance.id")
        .containsKey("telemetry.sdk.language")
        .containsKey("telemetry.sdk.name")
        .containsKey("telemetry.sdk.version");
    assertThat(provider.getResource().getSchemaUrl())
        .isEqualTo("https://opentelemetry.io/schemas/1.28.0");

    assertThat(EntityUtil.getEntities(provider.getResource()))
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e).hasType("service"), e -> assertThat(e).hasType("telemetry.sdk"));
  }
}
