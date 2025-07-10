/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.Attributes;
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
  private Attributes description;
  private Attributes id;
  @Nullable private String schemaUrl;

  SdkEntityBuilder(String entityType) {
    AttributeCheckUtil.isValid(entityType);
    this.entityType = entityType;
    this.description = Attributes.empty();
    this.id = Attributes.empty();
  }

  SdkEntityBuilder(Entity seed) {
    this.entityType = seed.getType();
    this.schemaUrl = seed.getSchemaUrl();
    this.id = seed.getId();
    this.description = seed.getDescription();
  }

  @Override
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public EntityBuilder withDescription(Attributes description) {
    AttributeCheckUtil.checkAttributes(description);
    this.description = description;
    return this;
  }

  @Override
  public EntityBuilder withId(Attributes id) {
    AttributeCheckUtil.checkAttributes(id);
    this.id = id;
    return this;
  }

  @Override
  public Entity build() {
    return SdkEntity.create(entityType, id, description, schemaUrl);
  }
}
