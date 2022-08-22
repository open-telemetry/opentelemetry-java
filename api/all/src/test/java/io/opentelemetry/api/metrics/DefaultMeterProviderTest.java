/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

public class DefaultMeterProviderTest {
  @Test
  void noopMeterProvider_get() {
    MeterProvider provider = MeterProvider.noop();
    assertThat(provider.get("user-instrumentation")).isInstanceOf(DefaultMeter.class);
  }

  @Test
  void noopMeterProvider_builder() {
    MeterProvider provider = MeterProvider.noop();
    provider.meterBuilder("test").build();
    provider.meterBuilder("test").setInstrumentationVersion("1.0").build();
    provider.meterBuilder("test").setSchemaUrl("myschema://url").build();
    provider
        .meterBuilder("test")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .build();
    provider
        .meterBuilder("test")
        .setInstrumentationVersion("1.0")
        .setSchemaUrl("myschema://url")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .build();
  }
}
