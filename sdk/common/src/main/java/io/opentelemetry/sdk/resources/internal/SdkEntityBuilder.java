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
final class SdkEntityBuilder implements EntityBuilder {
  private final String entityType;
  private final AttributesBuilder descriptionBuilder;
  private final AttributesBuilder idBuilder;
  @Nullable private String schemaUrl;

  SdkEntityBuilder(String entityType) {
    this.entityType = entityType;
    this.descriptionBuilder = Attributes.builder();
    this.idBuilder = Attributes.builder();
  }

  SdkEntityBuilder(Entity seed) {
    this.entityType = seed.getType();
    this.schemaUrl = seed.getSchemaUrl();
    this.idBuilder = seed.getId().toBuilder();
    this.descriptionBuilder = seed.getDescription().toBuilder();
  }

  @Override
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public EntityBuilder withDescription(Consumer<AttributesBuilder> f) {
    f.accept(this.descriptionBuilder);
    return this;
  }

  @Override
  public EntityBuilder withId(Consumer<AttributesBuilder> f) {
    f.accept(this.idBuilder);
    return this;
  }

  @Override
  public Entity build() {
    return SdkEntity.create(entityType, idBuilder.build(), descriptionBuilder.build(), schemaUrl);
  }
}
