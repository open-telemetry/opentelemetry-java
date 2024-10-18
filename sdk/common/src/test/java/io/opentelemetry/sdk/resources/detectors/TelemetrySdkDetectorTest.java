/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.detectors;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.resources.Entity;
import java.util.Collection;
import org.junit.Test;

/** Unit tests for {@link TelemetrySdkDetectorTest} */
public class TelemetrySdkDetectorTest {
  @Test
  void detects_telemetrySdk() {
    // Test we discovery telemetry.sdk.
    Collection<Entity> entities = TelemetrySdkDetector.INSTANCE.detectEntities();
    assertThat(entities).hasSize(1);
    Entity discovered = entities.iterator().next();
    assertThat(discovered.getSchemaUrl()).isNotBlank();
    // TODO - Pull stable strings from semconv here.
    // Possibly use codegen to make sure required attributes remain up-to-date.
    assertThat(discovered.getType()).isEqualTo("telemetry.sdk");
    assertThat(discovered.getIdentifyingAttributes()).containsKey("telemetry.sdk.version");
    assertThat(discovered.getIdentifyingAttributes()).containsKey("telemetry.sdk.name");
    assertThat(discovered.getIdentifyingAttributes()).containsKey("telemetry.sdk.language");
    assertThat(discovered.getAttributes()).isEmpty();
  }
}
