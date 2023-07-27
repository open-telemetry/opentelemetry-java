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
//    List<String> keys = new ArrayList<>(1000);
//    for (int i = 0; i < 10000; i++) {
//      keys.add("key"+i);
//    }
//    Map<String, String> pooledHashMap = new HashMap<>();
//    for (int i = 0; i < 100; i++) {
//      keys.forEach(key -> pooledHashMap.put(key, key));
//      pooledHashMap.clear();
//    }
  }
}
