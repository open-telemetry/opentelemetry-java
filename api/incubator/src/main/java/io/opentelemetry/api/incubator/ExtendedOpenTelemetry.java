/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.entities.Resource;
import io.opentelemetry.api.incubator.entities.ResourceProvider;

/** Extension to {@link OpenTelemetry} that adds {@link ResourceProvider}. */
public interface ExtendedOpenTelemetry extends OpenTelemetry {
  /** Returns the {@link ResourceProvider} for this {@link OpenTelemetry}. */
  default ResourceProvider getResourceProvider() {
    return ResourceProvider.noop();
  }

  /** Returns the {@link Resource} that telemetry from this {@link OpenTelemetry} uses. */
  default Resource getResource() {
    return getResourceProvider().getResource();
  }
}
