/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class EntityBuilder {
  @Nullable private String entityType;
  private final AttributesBuilder attributesBuilder;
  private final AttributesBuilder identifyingBuilder;
  @Nullable private String schemaUrl;

  EntityBuilder() {
    this.attributesBuilder = Attributes.builder();
    this.identifyingBuilder = Attributes.builder();
  }

  EntityBuilder(Entity seed) {
    this.entityType = seed.getType();
    this.schemaUrl = seed.getSchemaUrl();
    this.identifyingBuilder = seed.getIdentifyingAttributes().toBuilder();
    this.attributesBuilder = seed.getAttributes().toBuilder();
  }

  public EntityBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  public EntityBuilder setEntityType(String entityType) {
    this.entityType = entityType;
    return this;
  }

  public EntityBuilder withDescriptive(Consumer<AttributesBuilder> f) {
    f.accept(this.attributesBuilder);
    return this;
  }

  public EntityBuilder withIdentifying(Consumer<AttributesBuilder> f) {
    f.accept(this.identifyingBuilder);
    return this;
  }

  public Entity build() {
    // TODO - Better Checks.
    Objects.requireNonNull(this.entityType);
    return Entity.create(
        entityType, identifyingBuilder.build(), attributesBuilder.build(), schemaUrl);
  }
}
