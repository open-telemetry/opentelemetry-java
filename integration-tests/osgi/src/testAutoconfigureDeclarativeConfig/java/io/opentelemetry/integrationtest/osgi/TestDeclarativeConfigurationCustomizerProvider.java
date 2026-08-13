/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigurationCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

/**
 * Noop customizer provider that wraps the OTLP HTTP span exporter with a distinctive toString,
 * making the customizer's invocation verifiable in the SDK toString comparison.
 */
public class TestDeclarativeConfigurationCustomizerProvider
    implements DeclarativeConfigurationCustomizerProvider {

  @Override
  public void customize(DeclarativeConfigurationCustomizer customizer) {
    // SpanExporter.class matches all span exporters (OtlpHttpSpanExporter in this test).
    // OtlpHttpSpanExporter is final so we can't use it as type bound for the wrapper return type.
    customizer.addSpanExporterCustomizer(
        SpanExporter.class, (exporter, props) -> new TestCustomizedSpanExporter(exporter));
  }

  /** Wraps a SpanExporter with a distinctive toString to prove the customizer was invoked. */
  public static final class TestCustomizedSpanExporter implements SpanExporter {

    private final SpanExporter delegate;

    public TestCustomizedSpanExporter(SpanExporter delegate) {
      this.delegate = delegate;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return delegate.export(spans);
    }

    @Override
    public CompletableResultCode flush() {
      return delegate.flush();
    }

    @Override
    public CompletableResultCode shutdown() {
      return delegate.shutdown();
    }

    @Override
    public String toString() {
      return "TestDeclarativeCustomized{" + delegate + '}';
    }
  }
}
