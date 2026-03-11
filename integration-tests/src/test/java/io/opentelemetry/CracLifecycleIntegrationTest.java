/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration-style lifecycle tests for CRaC-like checkpoint/restore workflows.
 *
 * <p>In CRaC flows, applications typically need to close resources at checkpoint and resume normal
 * behavior after restore.
 */
class CracLifecycleIntegrationTest {

  @Test
  void exportsDoNotResumeAfterShutdown_currentBehavior() {
    LifecycleSpanExporter exporter = new LifecycleSpanExporter();
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                    .build())
            .build();

    try {
      Tracer tracer = sdk.getTracer("crac-lifecycle-test");

      emitSpan(tracer, "before-checkpoint");
      sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.exportedSpanCount()).isEqualTo(1);

      sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);

      // Simulate post-restore traffic on the same initialized SDK.
      emitSpan(tracer, "after-restore");
      sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);

      assertThat(exporter.exportedSpanCount()).isEqualTo(1);
    } finally {
      sdk.close();
    }
  }

  @Test
  @Disabled("Expected to fail until #6756 is addressed with checkpoint/restore-safe lifecycle")
  void exportsShouldResumeAfterRestore_expectedBehavior() {
    LifecycleSpanExporter exporter = new LifecycleSpanExporter();
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                    .build())
            .build();

    try {
      Tracer tracer = sdk.getTracer("crac-lifecycle-test");

      emitSpan(tracer, "before-checkpoint");
      sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
      assertThat(exporter.exportedSpanCount()).isEqualTo(1);

      sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);

      // Desired behavior for CRaC-style restore: post-restore spans should export again.
      emitSpan(tracer, "after-restore");
      sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);

      assertThat(exporter.exportedSpanCount()).isEqualTo(2);
    } finally {
      sdk.close();
    }
  }

  private static void emitSpan(Tracer tracer, String name) {
    Span span = tracer.spanBuilder(name).startSpan();
    span.end();
  }

  private static final class LifecycleSpanExporter implements SpanExporter {
    private final List<SpanData> exportedSpans = new ArrayList<>();
    private boolean shutdown;

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      if (shutdown) {
        return CompletableResultCode.ofFailure();
      }
      exportedSpans.addAll(spans);
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      shutdown = true;
      return CompletableResultCode.ofSuccess();
    }

    private int exportedSpanCount() {
      return exportedSpans.size();
    }
  }
}
