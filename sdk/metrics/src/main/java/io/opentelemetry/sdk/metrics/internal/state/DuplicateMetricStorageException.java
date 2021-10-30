/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;

/**
 * There are multiple metrics defined with the same name/identity.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
class DuplicateMetricStorageException extends IllegalArgumentException {
  private static final long serialVersionUID = 1547329629200005982L;
  private final MetricDescriptor existing;
  private final MetricDescriptor conflict;

  DuplicateMetricStorageException(
      MetricDescriptor existing, MetricDescriptor next, String message) {
    // TODO: Better error messages including async vs. sync instruments.
    super(message + " Found: " + existing + ", Want: " + next);
    this.existing = existing;
    this.conflict = next;
  }

  public MetricDescriptor getExisting() {
    return existing;
  }

  public MetricDescriptor getConflict() {
    return conflict;
  }
}
