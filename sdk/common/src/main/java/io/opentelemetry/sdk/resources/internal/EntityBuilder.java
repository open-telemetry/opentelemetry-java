/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * A builder of {@link Entity} that allows to add identifying or descriptive {@link Attributes}, as
 * well as type and schema_url.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class EntityBuilder {
  private final String entityType;
  private final AttributesBuilder attributesBuilder;
  private final AttributesBuilder identifyingBuilder;
  @Nullable private String schemaUrl;

  EntityBuilder(String entityType) {
    this.entityType = entityType;
    this.attributesBuilder = Attributes.builder();
    this.identifyingBuilder = Attributes.builder();
  }

  EntityBuilder(Entity seed) {
    this.entityType = seed.getType();
    this.schemaUrl = seed.getSchemaUrl();
    this.identifyingBuilder = seed.getIdentifyingAttributes().toBuilder();
    this.attributesBuilder = seed.getAttributes().toBuilder();
  }

  /**
   * Assign an OpenTelemetry schema URL to the resulting Entity.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used to create this Entity.
   * @return this
   */
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /**
   * Modify the descriptive attributes of this Entity.
   *
   * @param f A thunk which manipulates descriptive attributes.
   * @return this
   */
  public EntityBuilder withDescriptive(Consumer<AttributesBuilder> f) {
    f.accept(this.attributesBuilder);
    return this;
  }

  /**
   * Modify the identifying attributes of this Entity.
   *
   * @param f A thunk which manipulates identifying attributes.
   * @return this
   */
  public EntityBuilder withIdentifying(Consumer<AttributesBuilder> f) {
    f.accept(this.identifyingBuilder);
    return this;
  }

  /** Create the {@link Entity} from this. */
  public Entity build() {
    // TODO - Better Checks, e.g. identifying attributes are non-zero.
    return Entity.create(
        entityType, identifyingBuilder.build(), attributesBuilder.build(), schemaUrl);
  }
}
