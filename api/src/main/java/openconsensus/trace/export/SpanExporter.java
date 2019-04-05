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

import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.trace.Span;
import openconsensus.trace.data.TraceOptions;

/**
 * A service that is used by the library to export {@code SpanData} for all the spans that are part
 * of a distributed sampled trace (see {@link TraceOptions#isSampled()}).
 *
 * @since 0.5
 */
@ThreadSafe
public abstract class SpanExporter {
  private static final SpanExporter NOOP_SPAN_EXPORTER = new NoopSpanExporter();

  /**
   * Returns the no-op implementation of the {@code ExportComponent}.
   *
   * @return the no-op implementation of the {@code ExportComponent}.
   * @since 0.5
   */
  public static SpanExporter getNoopSpanExporter() {
    return NOOP_SPAN_EXPORTER;
  }

  /**
   * Registers a new service handler that is used by the library to export {@code SpanData} for
   * sampled spans (see {@link TraceOptions#isSampled()}).
   *
   * @param name the name of the service handler. Must be unique for each service.
   * @param handler the service handler that is called for each ended sampled span.
   * @since 0.5
   */
  public abstract void registerHandler(String name, Handler handler);

  /**
   * Unregisters the service handler with the provided name.
   *
   * @param name the name of the service handler that will be unregistered.
   * @since 0.5
   */
  public abstract void unregisterHandler(String name);

  /**
   * Exports a list of sampled (see {@link TraceOptions#isSampled()}) {@link Span}s using the
   * immutable representation {@link SpanData}.
   *
   * <p>This may be called from a different thread than the one that called {@link Span#end()}.
   *
   * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
   * different thread if possible.
   *
   * @param spanDataList a list of {@code SpanData} objects to be exported.
   * @since 0.5
   */
  public abstract void export(Collection<SpanData> spanDataList);

  /**
   * An abstract class that allows different tracing services to export recorded data for sampled
   * spans in their own format.
   *
   * <p>To export data this MUST be register to to the ExportComponent using {@link
   * #registerHandler(String, Handler)}.
   *
   * @since 0.5
   */
  public abstract static class Handler {

    /**
     * Exports a list of sampled (see {@link TraceOptions#isSampled()}) {@link Span}s using the
     * immutable representation {@link SpanData}.
     *
     * <p>This may be called from a different thread than the one that called {@link Span#end()}.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param spanDataList a list of {@code SpanData} objects to be exported.
     * @since 0.5
     */
    public abstract void export(Collection<SpanData> spanDataList);
  }

  private static final class NoopSpanExporter extends SpanExporter {

    @Override
    public void registerHandler(String name, Handler handler) {}

    @Override
    public void unregisterHandler(String name) {}

    @Override
    public void export(Collection<SpanData> spanDataList) {}
  }
}
