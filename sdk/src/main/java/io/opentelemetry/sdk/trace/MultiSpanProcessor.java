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

import io.opentelemetry.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the {@code SpanProcessor} that simply forwards all received events to a list of
 * {@code SpanProcessor}s.
 */
public final class MultiSpanProcessor implements SpanProcessor {
  private final List<SpanProcessor> spanProcessors;

  /**
   * Creates a new {@code MultiSpanProcessor}.
   *
   * @param spanProcessorList the {@code List} of {@code SpanProcessor}s.
   * @return a new {@code MultiSpanProcessor}.
   * @throws NullPointerException if the {@code spanProcessorList} is {@code null}.
   */
  public static SpanProcessor create(List<SpanProcessor> spanProcessorList) {
    return new MultiSpanProcessor(
        Collections.unmodifiableList(
            new ArrayList<>(Utils.checkNotNull(spanProcessorList, "spanProcessorList"))));
  }

  @Override
  public void onStart(ReadableSpan readableSpan) {
    for (SpanProcessor spanProcessor : spanProcessors) {
      spanProcessor.onStart(readableSpan);
    }
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {
    for (SpanProcessor spanProcessor : spanProcessors) {
      spanProcessor.onEnd(readableSpan);
    }
  }

  @Override
  public void shutdown() {
    for (SpanProcessor spanProcessor : spanProcessors) {
      spanProcessor.shutdown();
    }
  }

  @Override
  public void forceFlush() {
    for (SpanProcessor spanProcessor : spanProcessors) {
      spanProcessor.forceFlush();
    }
  }

  private MultiSpanProcessor(List<SpanProcessor> spanProcessors) {
    this.spanProcessors = spanProcessors;
  }
}
