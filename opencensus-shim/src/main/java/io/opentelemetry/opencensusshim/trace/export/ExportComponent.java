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

package io.opentelemetry.opencensusshim.trace.export;

import io.opentelemetry.opencensusshim.trace.TraceOptions;

/**
 * Class that holds the implementation instances for {@link SpanExporter}, {@link RunningSpanStore}
 * and {@link SampledSpanStore}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 *
 * @since 0.1.0
 */
public abstract class ExportComponent {

  /**
   * Returns the no-op implementation of the {@code ExportComponent}.
   *
   * @return the no-op implementation of the {@code ExportComponent}.
   * @since 0.1.0
   */
  public static ExportComponent newNoopExportComponent() {
    return new NoopExportComponent();
  }

  /**
   * Returns the {@link SpanExporter} which can be used to register handlers to export all the spans
   * that are part of a distributed sampled trace (see {@link TraceOptions#isSampled()}).
   *
   * @return the implementation of the {@code SpanExporter} or no-op if no implementation linked in
   *     the binary.
   * @since 0.1.0
   */
  public abstract SpanExporter getSpanExporter();

  /**
   * Returns the {@link RunningSpanStore} that can be used to get useful debugging information about
   * all the current active spans.
   *
   * @return the {@code RunningSpanStore}.
   * @since 0.1.0
   */
  public abstract RunningSpanStore getRunningSpanStore();

  /**
   * Returns the {@link SampledSpanStore} that can be used to get useful debugging information, such
   * as latency based sampled spans, error based sampled spans.
   *
   * @return the {@code SampledSpanStore}.
   * @since 0.1.0
   */
  public abstract SampledSpanStore getSampledSpanStore();

  /**
   * Will shutdown this ExportComponent after flushing any pending spans.
   *
   * @since 0.1.0
   */
  public void shutdown() {}

  private static final class NoopExportComponent extends ExportComponent {
    private final SampledSpanStore noopSampledSpanStore =
        SampledSpanStore.newNoopSampledSpanStore();

    @Override
    public SpanExporter getSpanExporter() {
      return SpanExporter.getNoopSpanExporter();
    }

    @Override
    public RunningSpanStore getRunningSpanStore() {
      return RunningSpanStore.getNoopRunningSpanStore();
    }

    @Override
    public SampledSpanStore getSampledSpanStore() {
      return noopSampledSpanStore;
    }
  }
}
