/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * SDK metrics exported for emitted logs as defined in the <a
 * href="https://opentelemetry.io/docs/specs/semconv/otel/sdk-metrics/#log-metrics">semantic
 * conventions</a>.
 */
final class SdkLoggerInstrumentation {
  private final Object lock = new Object();

  private final Supplier<MeterProvider> meterProvider;

  @Nullable private Meter meter;
  @Nullable private volatile LongCounter createdLogs;

  SdkLoggerInstrumentation(Supplier<MeterProvider> meterProvider) {
    this.meterProvider = meterProvider;
  }

  void emitLog() {
    createdLogs().add(1);
  }

  private LongCounter createdLogs() {
    LongCounter createdLogs = this.createdLogs;
    if (createdLogs == null) {
      synchronized (lock) {
        createdLogs = this.createdLogs;
        if (createdLogs == null) {
          createdLogs =
              meter()
                  .counterBuilder("otel.sdk.log.created")
                  .setUnit("{log_record}")
                  .setDescription("The number of logs submitted to enabled SDK Loggers.")
                  .build();
          this.createdLogs = createdLogs;
        }
      }
    }
    return createdLogs;
  }

  private Meter meter() {
    if (meter == null) {
      // Safe to call from multiple threads.
      meter = meterProvider.get().get("io.opentelemetry.sdk.logs");
    }
    return meter;
  }
}
