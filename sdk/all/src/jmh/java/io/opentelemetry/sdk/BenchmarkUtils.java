/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

public class BenchmarkUtils {

  /**
   * The number of record operations per benchmark invocation. By using a constant across benchmarks
   * of different signals, it's easier to compare benchmark results across signals.
   */
  public static final int RECORDS_PER_INVOCATION = 1024 * 10;
}
