/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

interface LongAggregator {
  void record(long recording);

  Accumulation collect();
}
