/*
 * Copyright 2017, OpenConsensus Authors
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

package openconsensus.trace.export;

import openconsensus.trace.data.TraceOptions;

/**
 * Class that holds the implementation instances for {@link SpanExporter}.
 *
 * <p>Unless otherwise noted all methods (on component) results are cacheable.
 *
 * @since 0.5
 */
public abstract class ExportComponent {

  /**
   * Returns the no-op implementation of the {@code ExportComponent}.
   *
   * @return the no-op implementation of the {@code ExportComponent}.
   * @since 0.5
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
   * @since 0.5
   */
  public abstract SpanExporter getSpanExporter();

  /**
   * Will shutdown this ExportComponent after flushing any pending spans.
   *
   * @since 0.14
   */
  public void shutdown() {}

  private static final class NoopExportComponent extends ExportComponent {

    @Override
    public SpanExporter getSpanExporter() {
      return SpanExporter.getNoopSpanExporter();
    }
  }
}
