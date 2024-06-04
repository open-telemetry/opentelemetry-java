/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.junit5;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.sdk.OpenTelemetryTestSdk;
import io.opentelemetry.sdk.testing.sdk.OpenTelemetryTesting;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit5 extension which sets up the {@link OpenTelemetrySdk} for testing, resetting state
 * between tests.
 *
 * <pre>{@code
 * // class CoolTest {
 * //   @RegisterExtension
 * //   static final OpenTelemetryExtension otelTesting = OpenTelemetryExtension.create();
 * //
 * //   private final Tracer tracer = otelTesting.getOpenTelemetry().getTracer("test");
 * //   private final Meter meter = otelTesting.getOpenTelemetry().getMeter("test");
 * //
 * //   @Test
 * //   void test() {
 * //     tracer.spanBuilder("name").startSpan().end();
 * //     assertThat(otelTesting.getSpans()).containsExactly(expected);
 * //
 * //     LongCounter counter = meter.counterBuilder("counter-name").build();
 * //     counter.add(1);
 * //     assertThat(otelTesting.getMetrics()).satisfiesExactlyInAnyOrder(metricData -> {});
 * //   }
 * // }
 * }</pre>
 */
public final class OpenTelemetryExtension extends OpenTelemetryTesting
    implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback {

  /**
   * Returns a {@link OpenTelemetryExtension} with a default SDK initialized with an in-memory span
   * exporter and W3C trace context propagation.
   */
  public static OpenTelemetryExtension create() {

    return new OpenTelemetryExtension(OpenTelemetryTestSdk.create());
  }

  private OpenTelemetryExtension(OpenTelemetryTestSdk openTelemetryTestSdk) {
    super(openTelemetryTestSdk);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    clearSpans();
    clearMetrics();
    clearLogRecords();
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    GlobalOpenTelemetry.resetForTest();
    GlobalOpenTelemetry.set(openTelemetry);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    GlobalOpenTelemetry.resetForTest();
    openTelemetry.close();
  }
}
