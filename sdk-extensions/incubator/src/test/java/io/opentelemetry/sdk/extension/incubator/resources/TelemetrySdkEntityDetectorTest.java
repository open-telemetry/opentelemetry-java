/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.resources.internal.Entity;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TelemetrySdkEntityDetectorTest {

  @Test
  void testDetect() {
    TelemetrySdkEntityDetector detector = new TelemetrySdkEntityDetector();
    Collection<Entity> entities =
        detector.detect(DefaultConfigProperties.createFromMap(Collections.emptyMap()));

    assertThat(entities).hasSize(1);
    Entity entity = entities.iterator().next();

    assertThat(entity.getType()).isEqualTo("telemetry.sdk");
    assertThat(entity.getId())
        .isEqualTo(
            Attributes.builder()
                .put("telemetry.sdk.name", "opentelemetry")
                .put("telemetry.sdk.language", "java")
                .build());
    assertThat(entity.getDescription())
        .isEqualTo(Attributes.builder().put("telemetry.sdk.version", OtelVersion.VERSION).build());
    assertThat(entity.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.40.0");
  }
}
