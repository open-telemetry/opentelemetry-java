/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/** The current state of an Entity. */
public interface EntityState {
  /** Returns the type of the Entity. */
  String getType();

  /** Returns the schema_url of the Entity, or null. */
  @Nullable
  String getSchemaUrl();

  /** Returns the identity of the Entity. */
  Attributes getId();

  /** Returns the description of the Entity. */
  Attributes getDescription();
}
