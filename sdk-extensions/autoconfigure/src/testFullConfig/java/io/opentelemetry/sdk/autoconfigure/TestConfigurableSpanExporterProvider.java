/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

public class TestConfigurableSpanExporterProvider implements ConfigurableSpanExporterProvider {
  @Override
  public SpanExporter createExporter(ConfigProperties config) {
    return new TestSpanExporter(config.getBoolean("should.always.fail"));
  }

  @Override
  public String getName() {
    return "testExporter";
  }

  public static class TestSpanExporter implements SpanExporter {

    private final boolean shouldAlwaysFail;

    public TestSpanExporter(boolean shouldAlwaysFail) {
      this.shouldAlwaysFail = shouldAlwaysFail;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      return shouldAlwaysFail
          ? CompletableResultCode.ofFailure()
          : CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return shouldAlwaysFail
          ? CompletableResultCode.ofFailure()
          : CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return shouldAlwaysFail
          ? CompletableResultCode.ofFailure()
          : CompletableResultCode.ofSuccess();
    }
  }
}
