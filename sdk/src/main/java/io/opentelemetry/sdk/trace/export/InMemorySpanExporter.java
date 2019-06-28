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

public final class InMemorySpanExporter implements SpanExporter {
  private final List<Span> finishedSpanDataItems = new ArrayList<>();
  private boolean is_stopped = false;

  /**
   * Returns a {@code List} of the finished {@code Span}s, represented by {@code
   * io.opentelemetry.proto.trace.v1.Span}.
   */
  public List<Span> getFinishedSpanItems() {
    synchronized (this) {
      return Collections.unmodifiableList(new ArrayList<>(finishedSpanDataItems));
    }
  }

  /** Clears the internal {@code List} of finished {@code Span}s. */
  public void reset() {
    synchronized (this) {
      finishedSpanDataItems.clear();
    }
  }

  @Override
  public void export(List<Span> spans) {
    synchronized (this) {
      if (!is_stopped) {
        finishedSpanDataItems.addAll(spans);
      }
    }
  }

  @Override
  public void shutdown() {
    synchronized (this) {
      finishedSpanDataItems.clear();
      is_stopped = true;
    }
  }
}
