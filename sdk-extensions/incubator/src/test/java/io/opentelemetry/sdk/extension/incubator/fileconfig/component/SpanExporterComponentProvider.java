/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

public class SpanExporterComponentProvider implements ComponentProvider<SpanExporter> {
  @Override
  public Class<SpanExporter> getType() {
    return SpanExporter.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public SpanExporter create(StructuredConfigProperties config) {
    return new TestSpanExporter(config);
  }

  public static class TestSpanExporter implements SpanExporter {

    public final StructuredConfigProperties config;

    private TestSpanExporter(StructuredConfigProperties config) {
      this.config = config;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
