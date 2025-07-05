/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.function.Consumer;

/**
 * A builder of {@link Entity} that allows to add identifying or descriptive {@link Attributes}, as
 * well as type and schema_url.
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
   * @param f A {@link Consumer} which builds the descriptive attributes.
   * @return this
   */
  EntityBuilder withDescription(Consumer<AttributesBuilder> f);

  /**
   * Modify the identifying attributes of this Entity.
   *
   * @param f A {@link Consumer} which builds the identifying attributes.
   * @return this
   */
  EntityBuilder withId(Consumer<AttributesBuilder> f);

  /** Create the {@link Entity} from this. */
  Entity build();
}
