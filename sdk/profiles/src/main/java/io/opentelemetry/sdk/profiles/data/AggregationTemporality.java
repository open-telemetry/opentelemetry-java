/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.profiles.data;

/**
 * Specifies the method of aggregating metric values.
 *
 * <p>TODO: This is intentionally not the same as metrics/AggregationTemporality. For profiles.proto
 * 'v1experimental' version, this class is considered distinct from the pre-exiting
 * AggregationTemporality in metrics.proto. As the profiles.proto stabilises, they may be refactored
 * into a version in common.proto. Meanwhile the Java class structure reflects the .proto structure
 * in making distinct entities.
 *
 * <p>refs for refactoring discussion:
 *
 * @see
 *     "https://github.com/open-telemetry/opentelemetry-proto/blob/v1.3.0/opentelemetry/proto/metrics/v1/metrics.proto#L261"
 * @see
 *     "https://github.com/open-telemetry/opentelemetry-proto/blob/v1.3.0/opentelemetry/proto/profiles/v1experimental/pprofextended.proto#L147"
 * @see "https://github.com/open-telemetry/opentelemetry-proto/issues/547"
 * @see "https://github.com/open-telemetry/opentelemetry-proto/pull/534#discussion_r1552403726"
 * @see "profiles.proto::AggregationTemporality"
 */
public enum AggregationTemporality {

  /** The default AggregationTemporality, it MUST not be used. */
  UNSPECIFIED,

  /**
   * DELTA is an AggregationTemporality for a profiler which reports changes since last report time.
   */
  DELTA,

  /**
   * CUMULATIVE is an AggregationTemporality for a profiler which reports changes since a fixed
   * start time.
   */
  CUMULATIVE
}
