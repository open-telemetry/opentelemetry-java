/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.sdk.extension.incubator.resources.internal.SdkEntityBuilder;

/** An instance of an Entity. */
public interface Entity {
  /** Constructs a new builder for creating Entities. */
  static EntityBuilder builder(String entityType) {
    return new SdkEntityBuilder(entityType);
  }

  /** Converts this entity to a builder. */
  EntityBuilder toBuilder();
}
