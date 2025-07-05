/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.entities.Entity;
import io.opentelemetry.api.incubator.entities.EntityBuilder;
import java.util.function.Consumer;

final class PassthroughEntityBuilder implements EntityBuilder {
  private final io.opentelemetry.sdk.resources.internal.EntityBuilder builder;

  PassthroughEntityBuilder(io.opentelemetry.sdk.resources.internal.EntityBuilder builder) {
    this.builder = builder;
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
  public Entity build() {
    return new PassthroughEntity(builder.build());
  }
}
