/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;

interface LongAggregator<T extends Accumulation> {
  void record(long recording);

  T collect(Clock clock);

  void merge(T accumulation);
}
