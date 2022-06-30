/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import java.util.concurrent.TimeUnit;

class TestTimer {
  int count = 0;
  long totalTimeNanos = 0;

  void add(long time, TimeUnit unit) {
    count++;
    totalTimeNanos += unit.toNanos(time);
  }

  int getCount() {
    return count;
  }

  double getTotalTimeNanos() {
    return totalTimeNanos;
  }

  void reset() {
    count = 0;
    totalTimeNanos = 0;
  }
}
