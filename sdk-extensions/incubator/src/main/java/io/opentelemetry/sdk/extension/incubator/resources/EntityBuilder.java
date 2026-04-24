/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.Attributes;

/** A builder of {@link Entity}. */
public interface EntityBuilder {

  /** Sets the schema_url of the Entity. */
  EntityBuilder setSchemaUrl(String schemaUrl);

  /** Sets the identity of the Entity. */
  EntityBuilder setIdentity(Attributes identity);

  /** Sets the description of the Entity. */
  EntityBuilder setDescription(Attributes description);

  /** Builds an entity. */
  Entity build();
}
