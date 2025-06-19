/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Entity represents an object of interest associated with produced telemetry: traces, metrics or
 * logs.
 *
 * <p>For example, telemetry produced using OpenTelemetry SDK is normally associated with a Service
 * entity. Similarly, OpenTelemetry defines system metrics for a host. The Host is the entity we
 * want to associate metrics with in this case.
 *
 * <p>Entities may be also associated with produced telemetry indirectly. For example a service that
 * produces telemetry is also related with a process in which the service runs, so we say that the
 * Service entity is related to the Process entity. The process normally also runs on a host, so we
 * say that the Process entity is related to the Host entity.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
@AutoValue
public abstract class Entity {
  /**
   * Returns the entity type string of this entity. Must not be null.
   *
   * @return the entity type.
   */
  public abstract String getType();

  /**
   * Returns a map of attributes that identify the entity.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getId();

  /**
   * Returns a map of attributes that describe the entity.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getDescription();

  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null if this entity
   * does not abide by schema conventions (i.e. is custom).
   *
   * @return An OpenTelemetry schema URL.
   * @since 1.4.0
   */
  @Nullable
  public abstract String getSchemaUrl();

  /**
   * Returns a {@link Entity}.
   *
   * @param entityType the entity type string of this entity.
   * @param id a map of attributes that identify the entity.
   * @param description a map of attributes that describe the entity.
   * @return a {@code Entity}.
   * @throws NullPointerException if {@code id} or {@code description} is null.
   * @throws IllegalArgumentException if entityType string, attribute key or attribute value is not
   *     a valid printable ASCII string or exceed {@link AttributeCheckUtil#MAX_LENGTH} characters.
   */
  static final Entity create(
      String entityType, Attributes id, Attributes description, @Nullable String schemaUrl) {
    AttributeCheckUtil.isValid(entityType);
    AttributeCheckUtil.checkAttributes(id);
    AttributeCheckUtil.checkAttributes(description);
    return new AutoValue_Entity(entityType, id, description, schemaUrl);
  }

  /**
   * Returns a new {@link EntityBuilder} instance populated with the data of this {@link Entity}.
   */
  public final EntityBuilder toBuilder() {
    return new EntityBuilder(this);
  }

  /**
   * Returns a new {@link EntityBuilder} instance for creating arbitrary {@link Entity}.
   *
   * @param entityType the entity type string of this entity.
   */
  public static final EntityBuilder builder(String entityType) {
    return new EntityBuilder(entityType);
  }
}
