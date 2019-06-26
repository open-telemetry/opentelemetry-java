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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@code SpanExporter} that simply forwards all received spans to a list of
 * {@code SpanExporter}.
 *
 * <p>Can be used to export to multiple backends using the same {@code SpanProcessor} like a {@code
 * SimpleSampledSpansProcessor} or a {@code BatchSampledSpansProcessor}.
 */
public final class MultiSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(MultiSpanExporter.class.getName());
  private final List<SpanExporter> spanExporters;

  static SpanExporter create(List<SpanExporter> spanExporters) {
    return new MultiSpanExporter(Collections.unmodifiableList(new ArrayList<>(spanExporters)));
  }

  @Override
  public void export(List<Span> spans) {
    for (SpanExporter spanExporter : spanExporters) {
      try {
        spanExporter.export(spans);
      } catch (Throwable t) {
        logger.log(Level.WARNING, "Exception thrown by the export.", t);
      }
    }
  }

  @Override
  public void shutdown() {
    for (SpanExporter spanExporter : spanExporters) {
      spanExporter.shutdown();
    }
  }

  private MultiSpanExporter(List<SpanExporter> spanExporters) {
    this.spanExporters = spanExporters;
  }
}
