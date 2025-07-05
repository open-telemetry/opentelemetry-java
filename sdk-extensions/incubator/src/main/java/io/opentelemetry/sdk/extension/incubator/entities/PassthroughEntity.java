/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.entities.Entity;
import io.opentelemetry.api.incubator.entities.EntityBuilder;
import javax.annotation.Nullable;

final class PassthroughEntity implements Entity {
  private final io.opentelemetry.sdk.resources.internal.Entity entity;

  PassthroughEntity(io.opentelemetry.sdk.resources.internal.Entity entity) {
    this.entity = entity;
  }

  io.opentelemetry.sdk.resources.internal.Entity getPassthrough() {
    return entity;
  }

  @Override
  public String getType() {
    return entity.getType();
  }

  @Override
  public Attributes getId() {
    return entity.getId();
  }

  @Override
  public Attributes getDescription() {
    return entity.getDescription();
  }

  @Override
  @Nullable
  public String getSchemaUrl() {
    return entity.getSchemaUrl();
  }

  @Override
  public EntityBuilder toBuilder() {
    return new PassthroughEntityBuilder(entity.toBuilder());
  }

  static EntityBuilder builder(String entityType) {
    return new PassthroughEntityBuilder(
        io.opentelemetry.sdk.resources.internal.Entity.builder(entityType));
  }
}
