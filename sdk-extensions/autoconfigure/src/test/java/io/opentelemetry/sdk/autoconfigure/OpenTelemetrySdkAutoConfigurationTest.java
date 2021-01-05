/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkAutoConfigurationTest {

  @Test
  void resourcePrioritizesUser() {
    Resource resource =
        OpenTelemetrySdkAutoConfiguration.configureResource(
            ConfigProperties.createForTest(
                Collections.singletonMap("otel.resource.attributes", "telemetry.sdk.name=test")));
    assertThat(resource.getAttributes().get(SemanticAttributes.TELEMETRY_SDK_NAME))
        .isEqualTo("test");
  }
}
