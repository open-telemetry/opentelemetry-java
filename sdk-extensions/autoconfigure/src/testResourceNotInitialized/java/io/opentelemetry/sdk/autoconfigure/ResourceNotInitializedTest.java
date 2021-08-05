/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

public class ResourceNotInitializedTest {
  @Test
  void shouldReturnEmptyResourceIfInitializeWasNotCalled() {
    Resource resource = OpenTelemetrySdkAutoConfiguration.getResource();
    assertTrue(resource.getAttributes().isEmpty());
  }
}
