/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.SdkLogEmitterProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LogEmitterProviderConfigurationTest {

  @Test
  void configureLogEmitterProvider() {
    Map<String, String> properties = Collections.singletonMap("otel.logs.exporter", "none");

    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    // We don't have any exporters on classpath for this test so check no-op case. Exporter cases
    // are verified in other test sets like testFullConfig.
    SdkLogEmitterProvider logEmitterProvider =
        LogEmitterProviderConfiguration.configureLogEmitterProvider(
            resource, DefaultConfigProperties.createForTest(properties), MeterProvider.noop());
    try {
      assertThat(logEmitterProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState -> {
                assertThat(sharedState).extracting("resource").isEqualTo(resource);
                assertThat(sharedState)
                    .extracting("logProcessor")
                    .isEqualTo(LogProcessor.composite());
              });
    } finally {
      logEmitterProvider.shutdown();
    }
  }
}
