/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.export.BatchSettings;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Builder of {@link SdkTracerProvider}. */
public final class SdkTracerProviderBuilder {
  private final List<SpanProcessor> spanProcessors = new ArrayList<>();

  private final List<SpanProcessor> exporterSpanProcessors = new ArrayList<>();

  private Clock clock = SystemClock.getInstance();
  private IdGenerator idsGenerator = IdGenerator.random();
  private Resource resource = Resource.getDefault();
  private Supplier<TraceConfig> traceConfigSupplier = TraceConfig::getDefault;

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkTracerProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Assign an {@link IdGenerator}.
   *
   * @param idGenerator A generator for trace and span ids. Note: this should be thread-safe and as
   *     contention free as possible.
   * @return this
   */
  public SdkTracerProviderBuilder setIdGenerator(IdGenerator idGenerator) {
    requireNonNull(idGenerator, "idGenerator");
    this.idsGenerator = idGenerator;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all Spans created by Tracers.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public SdkTracerProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Assign an initial {@link TraceConfig} that should be used with this SDK.
   *
   * @return this
   */
  public SdkTracerProviderBuilder setTraceConfig(TraceConfig traceConfig) {
    requireNonNull(traceConfig, "traceConfig");
    this.traceConfigSupplier = () -> traceConfig;
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link TraceConfig}. {@link TraceConfig} will be retrieved each
   * time a {@link io.opentelemetry.api.trace.Span} is started.
   *
   * @return this
   */
  public SdkTracerProviderBuilder setTraceConfig(Supplier<TraceConfig> traceConfigSupplier) {
    requireNonNull(traceConfigSupplier, "traceConfig");
    this.traceConfigSupplier = traceConfigSupplier;
    return this;
  }

  /**
   * Add a SpanProcessor to the span pipeline that will be built.
   *
   * @return this
   */
  public SdkTracerProviderBuilder addSpanProcessor(SpanProcessor spanProcessor) {
    spanProcessors.add(spanProcessor);
    return this;
  }

  /**
   * Adds a {@link SpanExporter} to the end of the span pipeline, enabling default batching of spans
   * before export. To configure batching or disable it, use {@link #addExporter(SpanExporter,
   * BatchSettings)} instead.
   */
  public SdkTracerProviderBuilder addExporter(SpanExporter exporter) {
    requireNonNull(exporter, "exporter");
    return addExporter(exporter, BatchSettings.builder().build());
  }

  /**
   * Adds a {@link SpanExporter} to the end of the span pipeline. {@link BatchSettings} will
   * configure the batching used before exporting spans. If {@link BatchSettings#noBatching()} is
   * provided, no batching will be done.
   */
  // Using deprecated for public use class
  @SuppressWarnings({"deprecation", "UnnecessarilyFullyQualified"})
  public SdkTracerProviderBuilder addExporter(SpanExporter exporter, BatchSettings batchSettings) {
    requireNonNull(exporter, "exporter");
    requireNonNull(batchSettings, "batchSettings");
    if (batchSettings == BatchSettings.noBatching()) {
      exporterSpanProcessors.add(
          io.opentelemetry.sdk.trace.export.SimpleSpanProcessor.create(exporter));
    } else {
      exporterSpanProcessors.add(
          io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(exporter)
              .setScheduleDelay(Duration.ofNanos(batchSettings.getScheduleDelayNanos()))
              .setExporterTimeout(Duration.ofNanos(batchSettings.getExporterTimeoutNanos()))
              .setMaxQueueSize(batchSettings.getMaxQueueSize())
              .setMaxExportBatchSize(batchSettings.getMaxExportBatchSize())
              .build());
    }
    return this;
  }

  /**
   * Create a new TraceSdkProvider instance.
   *
   * @return An initialized TraceSdkProvider.
   */
  public SdkTracerProvider build() {
    List<SpanProcessor> allSpanProcessors =
        new ArrayList<>(spanProcessors.size() + exporterSpanProcessors.size());
    allSpanProcessors.addAll(spanProcessors);
    allSpanProcessors.addAll(exporterSpanProcessors);
    return new SdkTracerProvider(
        clock, idsGenerator, resource, traceConfigSupplier, allSpanProcessors);
  }

  SdkTracerProviderBuilder() {}
}
