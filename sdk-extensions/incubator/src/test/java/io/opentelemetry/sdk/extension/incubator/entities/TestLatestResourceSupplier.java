/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class TestLatestResourceSupplier {

  @Test
  void getResource_defaultsAfterTimeout() {
    LatestResourceSupplier supplier = new LatestResourceSupplier(0);
    // This will block. We haven't registered our listener, so
    // we never get an initialize event. We should still get
    // a default resource.
    Resource resource = supplier.get();
    assertThat(resource.getAttributes()).containsKey("service.name");
  }
}
