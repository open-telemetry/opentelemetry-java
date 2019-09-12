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

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.TracerSdk;
import java.util.List;

/**
 * An interface that allows different tracing services to export recorded data for sampled spans in
 * their own format.
 *
 * <p>To export data this MUST be registered to the {@code TracerSdk} using a {@link
 * SimpleSampledSpansProcessor} or a {@code BatchSampledSpansProcessor}.
 */
// TODO: Change {@code BatchSampledSpansExporter} to {@link BatchSampledSpansExporter} when the
//  class is available.
public interface SpanExporter {

  /** The possible results for the export method. */
  enum ResultCode {
    /** The export operation finished successfully. */
    SUCCESS,

    /** The export operation finished with an error, but retrying may succeed. */
    FAILED_RETRYABLE,

    /**
     * The export operation finished with an error, the caller should not try to export the same
     * data again.
     */
    FAILED_NOT_RETRYABLE
  }

  /**
   * Called to export sampled {@code Span}s.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the export.
   */
  ResultCode export(List<Span> spans);

  /**
   * Called when {@link TracerSdk#shutdown()} is called, if this {@code SpanExporter} is register to
   * a {@code TracerSdk} object.
   */
  void shutdown();
}
