/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metricsv2.data.MetricData;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// todo: we need some sort of pull-based controller, as well.
class Controller {
  private final ScheduledExecutorService scheduler;
  private final Accumulator accumulator;
  private final Processor processor;
  private final MetricExporter exporter;
  private ScheduledFuture<?> scheduledFuture;

  Controller(Accumulator accumulator, Processor processor, MetricExporter exporter) {
    this.accumulator = accumulator;
    this.processor = processor;
    this.exporter = exporter;
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
  }

  void start() {
    if (scheduledFuture != null) {
      return;
    }
    scheduledFuture = scheduler.scheduleAtFixedRate(this::runOneCycle, 10, 10, TimeUnit.SECONDS);
  }

  CompletableResultCode shutdown() {
    // todo implement me for real, flush exporter, etc.
    scheduledFuture.cancel(false);
    scheduler.shutdown();
    return new CompletableResultCode().succeed();
  }

  // visible for testing only!
  void runOneCycle() {
    processor.start();
    accumulator.collectAndSendTo(processor);
    Collection<MetricData> dataToExport = processor.finish();
    exporter.export(dataToExport);
  }
}
