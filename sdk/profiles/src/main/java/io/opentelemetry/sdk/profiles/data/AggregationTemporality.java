/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

/**
 * Specifies the method of aggregating metric values.
 * @see "profiles.proto::AggregationTemporality"
 */
public enum AggregationTemporality {

  /**
   * The default AggregationTemporality, it MUST not be used.
   */
  UNSPECIFIED,

  /**
   * DELTA is an AggregationTemporality for a profiler which reports changes since last report time.
   */
  DELTA,

  /**
   * CUMULATIVE is an AggregationTemporality for a profiler which reports changes since a fixed start time.
   */
  CUMULATIVE
}
