/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

/**
 * Defines the self-monitoring telemetry SDK components should capture.
 *
 * @since 1.51.0
 */
public enum InternalTelemetryVersion {
  /**
   * Record self-monitoring metrics defined in the SDK prior the standardization in semantic
   * conventions.
   */
  LEGACY,
  /**
   * Record self-monitoring metrics defined in the latest semantic conventions version supported by
   * this SDK version.
   */
  LATEST
}
