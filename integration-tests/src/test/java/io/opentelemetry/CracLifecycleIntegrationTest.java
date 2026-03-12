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
import org.crac.Context;
import org.crac.Resource;
import org.junit.jupiter.api.Test;

/**
 * Integration-style lifecycle tests for CRaC (Coordinated Restore at Checkpoint) support.
 *
 * <p>These tests use {@link MockCracContext} to simulate the CRaC checkpoint/restore lifecycle
 * without a CRaC-enabled JDK. Resources register with the mock context; the test then drives {@code
 * beforeCheckpoint} and {@code afterRestore} callbacks directly.
 *
 * <p>See: <a href="https://github.com/open-telemetry/opentelemetry-java/issues/6756">#6756</a>
 */
class CracLifecycleIntegrationTest {

  /**
   * Demonstrates the failure mode when the SDK is naively shut down at checkpoint with no
   * corresponding restore logic. This is what happens today without proper CRaC support: the SDK is
   * a one-shot object, so spans emitted after a restore are silently dropped.
   */
  @Test
  void spansDroppedAfterRestore_naiveCracIntegration() throws Exception {
    MockCracContext cracContext = new MockCracContext();
    InMemorySpanExporter exporter = new InMemorySpanExporter();
    OpenTelemetrySdk sdk = buildSdk(exporter);
    Tracer tracer = sdk.getTracer("crac-lifecycle-test");

    // Naive CRaC resource: shuts the SDK down at checkpoint, does nothing on restore.
    cracContext.register(
        new Resource() {
          @Override
          public void beforeCheckpoint(Context<? extends Resource> context) {
            sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);
          }

          @Override
          public void afterRestore(Context<? extends Resource> context) {
            // No restore logic — this is the gap that #6756 addresses.
          }
        });

    emitSpan(tracer, "before-checkpoint");
    sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.exportedCount()).isEqualTo(1);

    cracContext.simulateCheckpoint();
    cracContext.simulateRestore();

    // Post-restore span is silently dropped: the SDK is shut down and has no way to reinitialize.
    emitSpan(tracer, "after-restore");
    sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.exportedCount()).isEqualTo(1);
  }

  /**
   * Describes the desired behavior once the SDK properly implements {@link Resource}: spans emitted
   * after a CRaC restore should be exported normally.
   *
   * <p>This test is disabled until <a
   * href="https://github.com/open-telemetry/opentelemetry-java/issues/6756">#6756</a> is addressed.
   * When that work lands, the SDK (or an adapter it exposes) should register with the CRaC context
   * so that {@code beforeCheckpoint} flushes and quiesces, and {@code afterRestore} reinitializes
   * exporters and processors. Replace the TODO below with the real SDK API.
   */
  @Test
  // @Disabled("Expected to fail until #6756 adds checkpoint/restore-safe SDK lifecycle")
  void spansExportedAfterRestore_properCracIntegration() throws Exception {
    MockCracContext cracContext = new MockCracContext();
    InMemorySpanExporter exporter = new InMemorySpanExporter();
    OpenTelemetrySdk sdk = buildSdk(exporter);
    Tracer tracer = sdk.getTracer("crac-lifecycle-test");

    // TODO(#6756): replace this placeholder with the real SDK CRaC API, e.g.:
    //   cracContext.register(sdk.asCracResource());
    cracContext.register(
        new Resource() {
          @Override
          public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
            sdk.getSdkTracerProvider().shutdown().join(10, TimeUnit.SECONDS);
          }

          @Override
          public void afterRestore(Context<? extends Resource> context) throws Exception {
            // Reinitialize: reopen connections, restart background threads.
            // No SDK API exists for this yet — this is the body of #6756.
          }
        });

    emitSpan(tracer, "before-checkpoint");
    sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.exportedCount()).isEqualTo(1);

    cracContext.simulateCheckpoint();
    cracContext.simulateRestore();

    emitSpan(tracer, "after-restore");
    sdk.getSdkTracerProvider().forceFlush().join(10, TimeUnit.SECONDS);
    assertThat(exporter.exportedCount()).isEqualTo(2);
  }

  private static OpenTelemetrySdk buildSdk(SpanExporter exporter) {
    return OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build())
        .build();
  }

  private static void emitSpan(Tracer tracer, String name) {
    Span span = tracer.spanBuilder(name).startSpan();
    span.end();
  }

  private static final class InMemorySpanExporter implements SpanExporter {
    private final List<SpanData> spans = new ArrayList<>();
    private boolean shutdown;

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
      if (shutdown) {
        return CompletableResultCode.ofFailure();
      }
      this.spans.addAll(spans);
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

    int exportedCount() {
      return spans.size();
    }
  }
}
