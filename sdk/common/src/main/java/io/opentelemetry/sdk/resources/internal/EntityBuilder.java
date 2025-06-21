/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.function.Consumer;

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
   * @param f A thunk which manipulates descriptive attributes.
   * @return this
   */
  EntityBuilder withDescription(Consumer<AttributesBuilder> f);

  /**
   * Modify the identifying attributes of this Entity.
   *
   * @param f A thunk which manipulates identifying attributes.
   * @return this
   */
  EntityBuilder withId(Consumer<AttributesBuilder> f);

  /** Create the {@link Entity} from this. */
  Entity build();
}
