/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

/**
 * Controls how multiple matching {@link io.opentelemetry.sdk.metrics.View}s are applied to an
 * instrument.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public enum ViewMatchingMode {
  /**
   * Each matching view creates a separate metric stream independently of any other views registered
   * for the same matching instrument.
   */
  INDEPENDENT,

  /** Matching views are combined (merged) to modify metric streams rather than duplicating them. */
  COMPOSABLE
}
