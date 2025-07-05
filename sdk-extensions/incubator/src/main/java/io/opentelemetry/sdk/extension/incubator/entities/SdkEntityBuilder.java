/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.entities.EntityBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import java.util.function.Consumer;

final class SdkEntityBuilder implements EntityBuilder {
  private final io.opentelemetry.sdk.resources.internal.EntityBuilder builder;
  private final Consumer<Entity> emitter;

  SdkEntityBuilder(String entityType, Consumer<Entity> emitter) {
    this.builder = Entity.builder(entityType);
    this.emitter = emitter;
  }

  @Override
  public EntityBuilder setSchemaUrl(String schemaUrl) {
    builder.setSchemaUrl(schemaUrl);
    return this;
  }

  @Override
  public EntityBuilder withDescription(Consumer<AttributesBuilder> f) {
    builder.withDescription(f);
    return this;
  }

  @Override
  public EntityBuilder withId(Consumer<AttributesBuilder> f) {
    builder.withId(f);
    return this;
  }

  @Override
  public void emit() {
    emitter.accept(builder.build());
  }
}
