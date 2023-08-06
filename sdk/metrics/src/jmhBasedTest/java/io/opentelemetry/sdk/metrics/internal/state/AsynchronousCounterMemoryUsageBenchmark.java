/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

public class AsynchronousCounterMemoryUsageBenchmark {

  private AsynchronousCounterMemoryUsageBenchmark() {
  }

  public static void main(String[] args) {
    AsynchronousMetricStorageMemoryProfilingBenchmark asynchronousMetricStorageMemoryProfilingBenchmark = new AsynchronousMetricStorageMemoryProfilingBenchmark();
    asynchronousMetricStorageMemoryProfilingBenchmark.measureMemoryUsage();
  }
}
