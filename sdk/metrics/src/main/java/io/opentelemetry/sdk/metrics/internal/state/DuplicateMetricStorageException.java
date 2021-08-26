/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;

/** There are multiple metrics defined with the same name/identity. */
class DuplicateMetricStorageException extends IllegalArgumentException {
  private static final long serialVersionUID = 1547329629200005982L;

  DuplicateMetricStorageException(
      MetricDescriptor existing, MetricDescriptor next, String message) {
    // TODO: Better error messages including async vs. sync instruments.
    super(message + " Found: " + existing + ", Want: " + next);
  }
}
