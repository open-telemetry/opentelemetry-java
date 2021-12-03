/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An interface that allows different tracing services to export recorded data for sampled spans in
 * their own format.
 *
 * <p>To export data this MUST be register to the {@code TracerSdk} using a {@link
 * SimpleSpanProcessor} or a {@code BatchSampledSpansProcessor}.
 */
public interface SpanExporter extends Closeable {

  /**
   * Returns a {@link SpanExporter} which delegates all exports to the {@code exporters} in order.
   *
   * <p>Can be used to export to multiple backends using the same {@link SpanProcessor} like a
   * {@link SimpleSpanProcessor} or a {@link BatchSpanProcessor}.
   */
  static SpanExporter composite(SpanExporter... exporters) {
    return composite(Arrays.asList(exporters));
  }

  /**
   * Returns a {@link SpanExporter} which delegates all exports to the {@code exporters} in order.
   *
   * <p>Can be used to export to multiple backends using the same {@link SpanProcessor} like a
   * {@link SimpleSpanProcessor} or a {@link BatchSpanProcessor}.
   */
  static SpanExporter composite(Iterable<SpanExporter> exporters) {
    List<SpanExporter> exportersList = new ArrayList<>();
    for (SpanExporter exporter : exporters) {
      exportersList.add(exporter);
    }
    if (exportersList.isEmpty()) {
      return NoopSpanExporter.getInstance();
    }
    if (exportersList.size() == 1) {
      return exportersList.get(0);
    }
    return MultiSpanExporter.create(exportersList);
  }

  /**
   * Called to export sampled {@code Span}s. Note that export operations can be performed
   * simultaneously depending on the type of span processor being used. However, the {@link
   * BatchSpanProcessor} will ensure that only one export can occur at a time.
   *
   * @param spans the collection of sampled Spans to be exported.
   * @return the result of the export, which is often an asynchronous operation.
   */
  CompletableResultCode export(Collection<SpanData> spans);

  /**
   * Exports the collection of sampled {@code Span}s that have not yet been exported. Note that
   * export operations can be performed simultaneously depending on the type of span processor being
   * used. However, the {@link BatchSpanProcessor} will ensure that only one export can occur at a
   * time.
   *
   * @return the result of the flush, which is often an asynchronous operation.
   */
  CompletableResultCode flush();

  /**
   * Called when {@link SdkTracerProvider#shutdown()} is called, if this {@code SpanExporter} is
   * registered to a {@link SdkTracerProvider} object.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes.
   */
  CompletableResultCode shutdown();

  /** Closes this {@link SpanExporter}, releasing any resources. */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
