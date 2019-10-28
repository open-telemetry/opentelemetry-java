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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;

/**
 * Interface that is used by the {@link TracerSdk} to generate new {@link SpanId} and {@link
 * TraceId}.
 */
public interface IdsGenerator {
  /**
   * Generates a new valid {@code SpanId}.
   *
   * @return a new valid {@code SpanId}.
   */
  SpanId generateSpanId();

  /**
   * Generates a new valid {@code TraceId}.
   *
   * @return a new valid {@code TraceId}.
   */
  TraceId generateTraceId();
}
