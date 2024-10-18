/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class Entity {
  /**
   * Returns the entity type string of this entity. May not be null.
   *
   * @return the entity type.
   */
  public abstract String getType();

  /**
   * Returns a map of attributes that identify the entity.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getIdentifyingAttributes();

  /**
   * Returns a map of attributes that describe the entity.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getAttributes();

  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null.
   *
   * @return An OpenTelemetry schema URL.
   * @since 1.4.0
   */
  @Nullable
  public abstract String getSchemaUrl();

  static final Entity create(
      String entityType,
      Attributes identifying,
      Attributes descriptive,
      @Nullable String schemaUrl) {
    return new AutoValue_Entity(entityType, identifying, descriptive, schemaUrl);
  }

  public final EntityBuilder toBuilder() {
    return new EntityBuilder(this);
  }

  public static final EntityBuilder builder() {
    return new EntityBuilder();
  }
}
