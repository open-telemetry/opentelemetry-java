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

package io.opentelemetry.opencensusshim.trace.config;

/**
 * Global configuration of the trace service. This allows users to change configs for the default
 * sampler, maximum events to be kept, etc. (see {@link TraceParams} for details).
 *
 * @since 0.1.0
 */
public abstract class TraceConfig {
  private static final NoopTraceConfig NOOP_TRACE_CONFIG = new NoopTraceConfig();

  /**
   * Returns the active {@code TraceParams}.
   *
   * @return the active {@code TraceParams}.
   * @since 0.1.0
   */
  public abstract TraceParams getActiveTraceParams();

  /**
   * Updates the active {@link TraceParams}.
   *
   * @param traceParams the new active {@code TraceParams}.
   * @since 0.1.0
   */
  public abstract void updateActiveTraceParams(TraceParams traceParams);

  /**
   * Returns the no-op implementation of the {@code TraceConfig}.
   *
   * @return the no-op implementation of the {@code TraceConfig}.
   * @since 0.1.0
   */
  public static TraceConfig getNoopTraceConfig() {
    return NOOP_TRACE_CONFIG;
  }

  private static final class NoopTraceConfig extends TraceConfig {

    @Override
    public TraceParams getActiveTraceParams() {
      return TraceParams.DEFAULT;
    }

    @Override
    public void updateActiveTraceParams(TraceParams traceParams) {}
  }
}
