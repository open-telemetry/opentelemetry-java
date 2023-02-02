/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.sdk.common.CompletableResultCode;
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
import zipkin2.Callback;
import zipkin2.Span;
import zipkin2.codec.BytesEncoder;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;

/**
 * This class was based on the <a
 * href="https://github.com/census-instrumentation/opencensus-java/tree/c960b19889de5e4a7b25f90919d28b066590d4f0/exporters/trace/zipkin">OpenCensus
 * zipkin exporter</a> code.
 */
public final class ZipkinSpanExporter implements SpanExporter {

  public static final Logger baseLogger = Logger.getLogger(ZipkinSpanExporter.class.getName());

  public static final String DEFAULT_ENDPOINT = "http://localhost:9411/api/v2/spans";

  private final ThrottlingLogger logger = new ThrottlingLogger(baseLogger);
  private final AtomicBoolean isShutdown = new AtomicBoolean();
  private final BytesEncoder<Span> encoder;
  private final Sender sender;
  private final ExporterMetrics exporterMetrics;

  private final OtelToZipkinSpanTransformer transformer;

  ZipkinSpanExporter(
      BytesEncoder<Span> encoder,
      Sender sender,
      Supplier<MeterProvider> meterProviderSupplier,
      OtelToZipkinSpanTransformer transformer) {
    this.encoder = encoder;
    this.sender = sender;
    this.exporterMetrics =
        sender.encoding() == Encoding.JSON
            ? ExporterMetrics.createHttpJson("zipkin", "span", meterProviderSupplier)
            : ExporterMetrics.createHttpProtobuf("zipkin", "span", meterProviderSupplier);
    this.transformer = transformer;
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spanDataList) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    int numItems = spanDataList.size();
    exporterMetrics.addSeen(numItems);

    List<byte[]> encodedSpans = new ArrayList<>(numItems);
    for (SpanData spanData : spanDataList) {
      Span zipkinSpan = transformer.generateSpan(spanData);
      encodedSpans.add(encoder.encode(zipkinSpan));
    }

    CompletableResultCode result = new CompletableResultCode();
    sender
        .sendSpans(encodedSpans)
        .enqueue(
            new Callback<Void>() {
              @Override
              public void onSuccess(Void value) {
                exporterMetrics.addSuccess(numItems);
                result.succeed();
              }

              @Override
              public void onError(Throwable t) {
                exporterMetrics.addFailed(numItems);
                logger.log(Level.WARNING, "Failed to export spans", t);
                result.fail();
              }
            });
    return result;
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

  /**
   * Returns a new Builder for {@link ZipkinSpanExporter}.
   *
   * @return a new {@link ZipkinSpanExporter}.
   */
  public static ZipkinSpanExporterBuilder builder() {
    return new ZipkinSpanExporterBuilder();
  }
}
