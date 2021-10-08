/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.SpanExporterWrapper;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.DelegatingSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class TestSpanExporterWrapper implements SpanExporterWrapper {
  @Override
  public SpanExporter wrap(SpanExporter delegate, ConfigProperties config) {
    return new WrappingSpanExporter(delegate) {
      @Override
      public CompletableResultCode export(Collection<SpanData> spans) {
        List<SpanData> wrapped =
            spans.stream()
                .map(
                    span ->
                        new DelegatingSpanData(span) {
                          @Override
                          public Attributes getAttributes() {
                            return span.getAttributes().toBuilder().put("wrapped", true).build();
                          }
                        })
                .collect(Collectors.toList());
        return delegate.export(wrapped);
      }
    };
  }

  abstract static class WrappingSpanExporter implements SpanExporter {

    private final SpanExporter delegate;

    WrappingSpanExporter(SpanExporter delegate) {
      this.delegate = delegate;
    }

    @Override
    public CompletableResultCode flush() {
      return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
      return delegate.shutdown();
    }
  }
}
