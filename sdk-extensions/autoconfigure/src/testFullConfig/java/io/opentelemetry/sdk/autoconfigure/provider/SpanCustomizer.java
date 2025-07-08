/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.stream.Collectors;

/** Behavior asserted in {@link io.opentelemetry.sdk.autoconfigure.FullConfigTest}. */
public class SpanCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addSpanProcessorCustomizer(SpanCustomizer::processorCustomizer);
    autoConfiguration.addSpanExporterCustomizer(SpanCustomizer::exporterCustomizer);
  }

  private static SpanProcessor processorCustomizer(
      SpanProcessor delegate, ConfigProperties config) {
    return new SpanProcessor() {
      @Override
      public void onStart(Context parentContext, ReadWriteSpan span) {
        span.setAttribute(AttributeKey.stringKey("extra-key"), "extra-value");
        if (delegate.isStartRequired()) {
          delegate.onStart(parentContext, span);
        }
      }

      @Override
      public boolean isStartRequired() {
        return true;
      }

      @Override
      public void onEnd(ReadableSpan span) {
        if (delegate.isEndRequired()) {
          delegate.onEnd(span);
        }
      }

      @Override
      public boolean isEndRequired() {
        return delegate.isEndRequired();
      }

      @Override
      public CompletableResultCode shutdown() {
        return delegate.shutdown();
      }

      @Override
      public CompletableResultCode forceFlush() {
        return delegate.forceFlush();
      }
    };
  }

  private static SpanExporter exporterCustomizer(SpanExporter delegate, ConfigProperties config) {
    return new SpanExporter() {
      @Override
      public CompletableResultCode export(Collection<SpanData> spans) {
        return delegate.export(
            spans.stream()
                .map(
                    span ->
                        new DelegatingSpanData(span) {
                          @Override
                          public Attributes getAttributes() {
                            return span.getAttributes().toBuilder()
                                .put("wrapped", config.getInt("otel.test.wrapped"))
                                .build();
                          }
                        })
                .collect(Collectors.toList()));
      }

      @Override
      public CompletableResultCode flush() {
        return delegate.flush();
      }

      @Override
      public CompletableResultCode shutdown() {
        return delegate.shutdown();
      }
    };
  }
}
