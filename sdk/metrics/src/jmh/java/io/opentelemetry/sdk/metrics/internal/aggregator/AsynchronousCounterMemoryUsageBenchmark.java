/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

public class AsynchronousCounterMemoryUsageBenchmark {

  private AsynchronousCounterMemoryUsageBenchmark() {
  }

  public static void main(String[] args) {
    AsynchronousCounterBenchmark asynchronousCounterBenchmark = new AsynchronousCounterBenchmark();
    asynchronousCounterBenchmark.measureMemoryUsage();
  }
}
