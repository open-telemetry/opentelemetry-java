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

package io.opentelemetry.sdk.trace.config;

import io.opencensus.trace.config.TraceParams;

/**
 * Global configuration of the trace service. This allows users to change configs for the maximum
 * events to be kept, etc. (see {@link TraceParams} for details).
 */
public interface TraceConfig {
  /**
   * Returns the active {@code TraceParams}.
   *
   * @return the active {@code TraceParams}.
   */
  TraceParams getActiveTraceParams();

  /**
   * Updates the active {@link TraceParams}.
   *
   * @param traceParams the new active {@code TraceParams}.
   */
  void updateActiveTraceParams(TraceParams traceParams);
}
