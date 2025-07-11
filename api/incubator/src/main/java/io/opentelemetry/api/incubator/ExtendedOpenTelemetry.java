/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.entities.EntityProvider;

/** Extension to {@link OpenTelemetry} that adds {@link EntityProvider}. */
public interface ExtendedOpenTelemetry extends OpenTelemetry {
  /** Returns the {@link EntityProvider} for this {@link OpenTelemetry}. */
  default EntityProvider getEntityProvider() {
    return EntityProvider.noop();
  }
}
