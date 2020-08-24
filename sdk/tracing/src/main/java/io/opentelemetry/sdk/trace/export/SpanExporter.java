/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collection;

/**
 * An interface that allows different tracing services to export recorded data for sampled spans in
 * their own format.
 *
 * <p>To export data this MUST be register to the {@code TracerSdk} using a {@link
 * SimpleSpanProcessor} or a {@code BatchSampledSpansProcessor}.
 */
public interface SpanExporter {

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
   * Called when {@link TracerSdkProvider#shutdown()} is called, if this {@code SpanExporter} is
   * register to a {@code TracerSdkProvider} object.
   */
  void shutdown();
}
