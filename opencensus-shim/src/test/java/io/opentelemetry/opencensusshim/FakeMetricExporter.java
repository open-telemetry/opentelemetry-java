/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2018, OpenCensus Authors
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

package io.opentelemetry.opencensusshim;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

class FakeMetricExporter implements MetricExporter {

  private final Object monitor = new Object();

  @GuardedBy("monitor")
  private List<List<MetricData>> exportedMetrics = new ArrayList<>();

  /**
   * Waits until export is called for numberOfExports times. Returns the list of exported lists of
   * metrics
   */
  @Nullable
  List<List<MetricData>> waitForNumberOfExports(int numberOfExports) {
    List<List<MetricData>> ret;
    synchronized (monitor) {
      while (exportedMetrics.size() < numberOfExports) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          // Preserve the interruption status as per guidance.
          Thread.currentThread().interrupt();
          return null;
        }
      }
      ret = exportedMetrics;
      exportedMetrics = new ArrayList<>();
    }
    return ret;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    synchronized (monitor) {
      this.exportedMetrics.add(new ArrayList<>(metrics));
      monitor.notifyAll();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return null;
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
