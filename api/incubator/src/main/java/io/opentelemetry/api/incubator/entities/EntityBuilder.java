/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

import io.opentelemetry.api.common.Attributes;

/**
 * A builder of an Entity that allows to add identifying or descriptive {@link Attributes}, as well
 * as type and schema_url.
 *
 * <p>Entity represents an object of interest associated with produced telemetry: traces, metrics or
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
 */
public interface EntityBuilder {
  /**
   * Assign an OpenTelemetry schema URL to the resulting Entity.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used to create this Entity.
   * @return this
   */
  EntityBuilder setSchemaUrl(String schemaUrl);

  /**
   * Modify the descriptive attributes of this Entity.
   *
   * @param description The {@link Attributes} which describe this Entity.
   * @return this
   */
  EntityBuilder withDescription(Attributes description);

  /**
   * Modify the identifying attributes of this Entity.
   *
   * @param id The {@link Attributes} which identify this Entity.
   * @return this
   */
  EntityBuilder withId(Attributes id);

  /** Emits the current entity. */
  void emit();
}
