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

import io.opentelemetry.sdk.trace.SpanData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

final class WaitingSpanExporter implements SpanExporter {
  private final Object monitor = new Object();

  @GuardedBy("monitor")
  private final List<SpanData> spanDataList = new ArrayList<>();

  /**
   * Waits until we received numberOfSpans spans to export. Returns the list of exported {@link
   * SpanData} objects, otherwise {@code null} if the current thread is interrupted.
   *
   * @param numberOfSpans the number of minimum spans to be collected.
   * @return the list of exported {@link SpanData} objects, otherwise {@code null} if the current
   *     thread is interrupted.
   */
  @Nullable
  List<SpanData> waitForExport(int numberOfSpans) {
    List<SpanData> ret;
    synchronized (monitor) {
      while (spanDataList.size() < numberOfSpans) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          // Preserve the interruption status as per guidance.
          Thread.currentThread().interrupt();
          return null;
        }
      }
      ret = new ArrayList<>(spanDataList);
      spanDataList.clear();
    }
    return ret;
  }

  @Override
  public ResultCode export(List<SpanData> spans) {
    synchronized (monitor) {
      this.spanDataList.addAll(spans);
      monitor.notifyAll();
    }
    return ResultCode.SUCCESS;
  }

  @Override
  public void shutdown() {
    // Do nothing;
  }
}
