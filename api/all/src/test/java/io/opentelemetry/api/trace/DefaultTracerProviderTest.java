/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class DefaultTracerProviderTest {

  @Test
  void noopTracerProvider_get() {
    assertThat(TracerProvider.noop().get("test")).isInstanceOf(DefaultTracer.class);
    assertThat(TracerProvider.noop().get("test", "1.0")).isInstanceOf(DefaultTracer.class);
  }

  @Test
  void noopTracerProvider_builder() {
    TracerProvider provider = TracerProvider.noop();
    provider.tracerBuilder("test").build();
    provider.tracerBuilder("test").setInstrumentationVersion("1.0").build();
    provider.tracerBuilder("test").setSchemaUrl("myschema://url").build();
    provider
        .tracerBuilder("test")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .build();
    provider
        .tracerBuilder("test")
        .setInstrumentationVersion("1.0")
        .setSchemaUrl("myschema://url")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .build();
  }
}
