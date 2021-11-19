/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import org.junit.jupiter.api.Test;

public class NoopMeterProviderTest {
  @Test
  void noopMeterProvider_getDoesNotThrow() {
    MeterProvider provider = MeterProvider.noop();
    provider.get("user-instrumentation");
  }

  @Test
  void noopMeterProvider_builderDoesNotThrow() {
    MeterProvider provider = MeterProvider.noop();
    provider.meterBuilder("user-instrumentation").build();
    provider.meterBuilder("advanced-instrumetnation").setInstrumentationVersion("1.0").build();
    provider.meterBuilder("schema-instrumentation").setSchemaUrl("myschema://url").build();
    provider
        .meterBuilder("schema-instrumentation")
        .setInstrumentationVersion("1.0")
        .setSchemaUrl("myschema://url")
        .build();
  }
}
