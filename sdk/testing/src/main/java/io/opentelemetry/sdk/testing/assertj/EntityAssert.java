/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.internal.Entity;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ThrowingConsumer;

/** Assertions for {@link Entity}. */
public class EntityAssert extends AbstractAssert<EntityAssert, Entity> {
  EntityAssert(@Nullable Entity actual) {
    super(actual, EntityAssert.class);
  }

  /** Asserts that the entity type is equal to a given string. */
  public EntityAssert hasType(String entityType) {
    assertThat(actual.getType()).isEqualTo(entityType);
    return this;
  }

  /** Asserts that the entity id satisfies the given asserts. */
  public EntityAssert hasIdSatisfying(ThrowingConsumer<Attributes> asserts) {
    asserts.accept(actual.getId());
    return this;
  }

  /** Asserts that the entity description satisfies the given asserts. */
  public EntityAssert hasDescriptionSatisfying(ThrowingConsumer<Attributes> asserts) {
    asserts.accept(actual.getDescription());
    return this;
  }

  /** Asserts that the entity schemaUrl is equal to a given string. */
  public EntityAssert hasSchemaUrl(String schemaUrl) {
    assertThat(actual.getSchemaUrl()).isEqualTo(schemaUrl);
    return this;
  }
}
