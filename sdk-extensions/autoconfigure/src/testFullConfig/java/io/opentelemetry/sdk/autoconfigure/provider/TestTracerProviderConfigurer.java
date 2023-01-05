/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;

@SuppressWarnings("deprecation") // Support testing of SdkTracerProviderConfigurer
public class TestTracerProviderConfigurer
    implements io.opentelemetry.sdk.autoconfigure.spi.traces.SdkTracerProviderConfigurer {
  @Override
  public void configure(SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
    tracerProvider.addSpanProcessor(
        new SpanProcessor() {
          @Override
          public void onStart(Context parentContext, ReadWriteSpan span) {
            span.setAttribute("configured", config.getBoolean("otel.test.configured"));
          }

          @Override
          public boolean isStartRequired() {
            return true;
          }

          @Override
          public void onEnd(ReadableSpan span) {}

          @Override
          public boolean isEndRequired() {
            return false;
          }
        });
  }
}
