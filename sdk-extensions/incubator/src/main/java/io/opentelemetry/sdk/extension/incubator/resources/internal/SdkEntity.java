/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.extension.incubator.resources.Entity;
import io.opentelemetry.sdk.extension.incubator.resources.EntityBuilder;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class SdkEntity implements Entity {

  private final String entityType;
  @Nullable private final String schemaUrl;
  private final Attributes identity;
  private final Attributes description;

  SdkEntity(
      String entityType, @Nullable String schemaUrl, Attributes identity, Attributes description) {
    this.entityType = entityType;
    this.schemaUrl = schemaUrl;
    this.identity = identity;
    this.description = description;
  }

  /**
   * Returns the entity type string of this entity. Must not be null.
   *
   * @return the entity type.
   */
  public String getType() {
    return entityType;
  }

  /**
   * Returns a map of attributes that identify the entity.
   *
   * @return the entity identity.
   */
  public Attributes getIdentity() {
    return this.identity;
  }

  /**
   * Returns a map of attributes that describe the entity.
   *
   * @return the entity description.
   */
  public Attributes getDescription() {
    return this.description;
  }

  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null if this entity
   * does not abide by schema conventions (i.e. is custom).
   *
   * @return An OpenTelemetry schema URL.
   */
  @Nullable
  public String getSchemaUrl() {
    return this.schemaUrl;
  }

  @Override
  public EntityBuilder toBuilder() {
    return new SdkEntityBuilder(this);
  }
}
