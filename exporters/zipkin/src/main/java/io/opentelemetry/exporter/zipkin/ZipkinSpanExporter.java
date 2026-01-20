/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import io.opentelemetry.api.internal.InstrumentationUtil;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.metrics.ExporterInstrumentation;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import zipkin2.Span;
import zipkin2.reporter.BytesEncoder;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.Encoding;

/**
 * This class was based on the <a
 * href="https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin">OpenCensus
 * zipkin exporter</a> code.
 *
 * @deprecated Zipkin exporter is deprecated in OpenTelemetry spec (see the <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/pull/4715">PR</a>).
 *     Expect this artifact to no longer be published in approximately 6 months (mid 2026).
 */
@Deprecated
public final class ZipkinSpanExporter implements SpanExporter {

  public static final Logger baseLogger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  public static final String DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";

  private final ThrottlingLogger logger = new ThrottlingLogger(baseLogger);
  private final AtomicBoolean isShutdown = new AtomicBoolean();
  private final ZipkinSpanExporterBuilder builder;
  private final BytesEncoder<Span> encoder;
  private final BytesMessageSender sender;
  private final ExporterInstrumentation exporterMetrics;

  private final OtelToZipkinSpanTransformer transformer;

  ZipkinSpanExporter(
      ZipkinSpanExporterBuilder builder,
      BytesEncoder<Span> encoder,
      BytesMessageSender sender,
      Supplier<MeterProvider> meterProviderSupplier,
      InternalTelemetryVersion internalTelemetryVersion,
      String endpoint,
      OtelToZipkinSpanTransformer transformer) {
    this.builder = builder;
    this.encoder = encoder;
    this.sender = sender;
    this.transformer = transformer;

    StandardComponentId.ExporterType exporterType;
    if (sender.encoding() == Encoding.JSON) {
      exporterType = StandardComponentId.ExporterType.ZIPKIN_HTTP_JSON_SPAN_EXPORTER;
    } else {
      exporterType = StandardComponentId.ExporterType.ZIPKIN_HTTP_SPAN_EXPORTER;
    }
    this.exporterMetrics =
        new ExporterInstrumentation(
            internalTelemetryVersion,
            meterProviderSupplier,
            ComponentId.generateLazy(exporterType),
            endpoint);
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spanDataList) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    int numItems = spanDataList.size();
    ExporterInstrumentation.Recording metricRecording =
        exporterMetrics.startRecordingExport(numItems);

    List<byte[]> encodedSpans = new ArrayList<>(numItems);
    for (SpanData spanData : spanDataList) {
      Span zipkinSpan = transformer.generateSpan(spanData);
      encodedSpans.add(encoder.encode(zipkinSpan));
    }

    CompletableResultCode resultCode = new CompletableResultCode();
    InstrumentationUtil.suppressInstrumentation(
        () -> {
          try {
            sender.send(encodedSpans);
            metricRecording.finishSuccessful();
            resultCode.succeed();
          } catch (IOException | RuntimeException e) {
            metricRecording.finishFailed(e);
            logger.log(Level.WARNING, "Failed to export spans", e);
            resultCode.fail();
          }
        });
    return resultCode;
  }

  @Override
  public CompletableResultCode flush() {
    // nothing required here
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    try {
      sender.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Exception while closing the Zipkin Sender instance", e);
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    return "ZipkinSpanExporter{" + builder.toString(false) + "}";
  }

  /**
   * Returns a new Builder for {@link ZipkinSpanExporter}.
   *
   * @return a new {@link ZipkinSpanExporter}.
   */
  public static ZipkinSpanExporterBuilder builder() {
    return new ZipkinSpanExporterBuilder();
  }
}
