/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

/** Defines the self-monitoring telemetry SDK components should capture. */
public enum InternalTelemetryVersion {
  /**
   * Record self-monitoring metrics defined in the SDK prior the standardization in semantic
   * conventions.
   */
  LEGACY,
  /** Record self-monitoring metrics defined in the the semantic conventions version 1.33. */
  V1_33,
  /** Record self-monitoring metrics defined in the latest semantic conventions version. */
  LATEST
}
