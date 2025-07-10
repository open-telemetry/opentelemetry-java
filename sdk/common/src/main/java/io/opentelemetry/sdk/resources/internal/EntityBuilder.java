/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;

/**
 * A builder of {@link Entity} that allows to add identifying or descriptive {@link Attributes}, as
 * well as type and schema_url.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface EntityBuilder {
  /**
   * Assign an OpenTelemetry schema URL to the resulting Entity.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used to create this Entity.
   * @return this
   */
  EntityBuilder setSchemaUrl(String schemaUrl);

  /**
   * Modify the descriptive attributes of this Entity.
   *
   * @param description The attributes that describe the Entity.
   * @return this
   */
  EntityBuilder withDescription(Attributes description);

  /**
   * Modify the identifying attributes of this Entity.
   *
   * @param id The identifying attributes.
   * @return this
   */
  EntityBuilder withId(Attributes id);

  /** Create the {@link Entity} from this. */
  Entity build();
}
