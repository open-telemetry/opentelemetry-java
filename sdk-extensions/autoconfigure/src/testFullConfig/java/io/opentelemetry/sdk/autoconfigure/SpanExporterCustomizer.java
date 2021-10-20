/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredOpenTelemetrySdkCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfiguredOpenTelemetrySdkCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.stream.Collectors;

public class SpanExporterCustomizer implements AutoConfiguredOpenTelemetrySdkCustomizerProvider {
  @Override
  public void customize(AutoConfiguredOpenTelemetrySdkCustomizer autoConfiguration) {
    autoConfiguration.addSpanExporterCustomizer(
        (delegate, config) ->
            new SpanExporter() {
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
            });
  }
}
