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

/** Unit tests for {@link ServiceInstanceDetector}. */
public class ServiceInstanceDetectorTest {
  @Test
  void detects_service() {
    // Test we discovery telemetry.sdk.
    Collection<Entity> entities = ServiceInstanceDetector.INSTANCE.detectEntities();
    assertThat(entities).hasSize(1);
    Entity discovered = entities.iterator().next();
    assertThat(discovered.getSchemaUrl()).isNotBlank();
    // Possibly use codegen to make sure required attributes remain up-to-date.
    assertThat(discovered.getType()).isEqualTo("service.instance");
    assertThat(discovered.getIdentifyingAttributes()).containsKey("service.instance.id");
  }
}
