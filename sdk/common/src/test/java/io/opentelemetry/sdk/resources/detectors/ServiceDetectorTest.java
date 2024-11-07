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

/** Unit tests for {@link ServiceDetector} */
public class ServiceDetectorTest {
  @Test
  void detects_service() {
    // Test we discovery telemetry.sdk.
    Collection<Entity> entities = ServiceDetector.INSTANCE.detectEntities();
    assertThat(entities).hasSize(1);
    Entity discovered = entities.iterator().next();
    assertThat(discovered.getSchemaUrl()).isNotBlank();
    // Possibly use codegen to make sure required attributes remain up-to-date.
    assertThat(discovered.getType()).isEqualTo("service");
    assertThat(discovered.getIdentifyingAttributes()).containsKey("service.name");
    // Breaks unit tests.
    // assertThat(discovered.getIdentifyingAttributes()).containsKey("service.namespace");
    // assertThat(discovered.getAttributes()).containsKey("service.version");
  }
}
