/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;

@SuppressWarnings("deprecation") // Support testing of SdkTracerProviderConfigurer
public class TestTracerProviderConfigurer
    implements io.opentelemetry.sdk.autoconfigure.spi.traces.SdkTracerProviderConfigurer {
  @Override
  public void configure(SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
    tracerProvider.addSpanProcessor(
        SpanProcessor.startOnly(
            (ctx, span) -> {
              span.setAttribute("configured", config.getBoolean("otel.test.configured"));
            }));
  }
}
