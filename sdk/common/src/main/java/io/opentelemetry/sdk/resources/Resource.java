/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.common.internal.SemConvConstants;
import io.opentelemetry.sdk.resources.internal.AttributeCheckUtil;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which captures identifying information about the entities
 * for which signals (stats or traces) are reported.
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final Logger logger = Logger.getLogger(Resource.class.getName());

  private static final Resource EMPTY = create(Attributes.empty());

  /**
   * The MANDATORY Resource instance contains the mandatory attributes that must be used if they are
   * not provided by the Resource that is given to an SDK signal provider.
   */
  private static final Resource MANDATORY =
      create(Attributes.of(SemConvConstants.SERVICE_NAME, "unknown_service:java"));

  private static final Resource TELEMETRY_SDK =
      builder()
          .addEntity(
              Entity.builder(
                      SemConvConstants.TELEMETRY_SDK_TYPE,
                      Attributes.of(
                          SemConvConstants.TELEMETRY_SDK_NAME,
                          "opentelemetry",
                          SemConvConstants.TELEMETRY_SDK_LANGUAGE,
                          "java"))
                  .setDescription(
                      Attributes.of(SemConvConstants.TELEMETRY_SDK_VERSION, OtelVersion.VERSION))
                  .setSchemaUrl(SemConvConstants.SCHEMA_URL_V1_40_0)
                  .build())
          .build();

  // TODO(jack-berg): Explicitly null out schema url to avoid avoid assigning schemaUrl to default
  // resource which previously had none. Revisit when entities stabilize.
  private static final Resource DEFAULT =
      builder().putAll(MANDATORY).putAll(TELEMETRY_SDK).buildWithSchemaUrl(null);

  /**
   * Returns the default {@link Resource}. This resource contains the default attributes provided by
   * the SDK, with the telemetry SDK attributes associated with an experimental {@code
   * telemetry.sdk} entity.
   *
   * @return a {@code Resource}.
   */
  public static Resource getDefault() {
    return DEFAULT;
  }

  /**
   * Returns an empty {@link Resource}. When creating a {@link Resource}, it is strongly recommended
   * to start with {@link Resource#getDefault()} instead of this method to include SDK required
   * attributes.
   *
   * @return an empty {@code Resource}.
   */
  public static Resource empty() {
    return EMPTY;
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of attributes that describe the resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if an attribute key is empty, is not printable ASCII, or
   *     exceeds 255 characters.
   */
  public static Resource create(Attributes attributes) {
    return create(attributes, null);
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of {@link Attributes} that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if an attribute key is empty, is not printable ASCII, or
   *     exceeds 255 characters.
   */
  public static Resource create(Attributes attributes, @Nullable String schemaUrl) {
    return create(attributes, schemaUrl, Collections.emptyList());
  }

  /**
   * Builds a resource after conflicts have been resolved. Entity types and attribute key owners
   * must be unique, and ordinary attribute keys must not also belong to entities. This method
   * rejects conflicting input rather than choosing which value wins; callers must merge ordinary
   * attributes first and entities second before calling it.
   *
   * @param attributes unassociated attributes that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @param entities The normalized set of valid entities that participate in this resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute keys are invalid or the state is not normalized.
   */
  static Resource create(
      Attributes attributes, @Nullable String schemaUrl, Collection<Entity> entities) {
    AttributeCheckUtil.checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    Collection<Entity> immutableEntities = Collections.unmodifiableList(new ArrayList<>(entities));
    Set<String> entityTypes = new HashSet<>();
    Set<String> entityKeys = new HashSet<>();
    AttributesBuilder fullAttributes = Attributes.builder();
    for (Entity entity : immutableEntities) {
      if (!entityTypes.add(entity.getType())) {
        throw new IllegalArgumentException("Entity types must be unique in normalized resources.");
      }
      putEntityAttributes(entity.getId(), entityKeys, fullAttributes);
      putEntityAttributes(entity.getDescription(), entityKeys, fullAttributes);
    }
    attributes.forEach(
        (key, value) -> {
          if (entityKeys.contains(key.getKey())) {
            throw new IllegalArgumentException(
                "Unassociated attributes must not overlap entity attributes.");
          }
        });
    fullAttributes.putAll(attributes);
    return new AutoValue_Resource(schemaUrl, immutableEntities, fullAttributes.build());
  }

  private static void putEntityAttributes(
      Attributes attributes, Set<String> entityKeys, AttributesBuilder fullAttributes) {
    attributes.forEach(
        (key, value) -> {
          if (!entityKeys.add(key.getKey())) {
            throw new IllegalArgumentException(
                "Entity attribute keys must have a single owner and classification.");
          }
        });
    fullAttributes.putAll(attributes);
  }

  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null.
   *
   * @return An OpenTelemetry schema URL.
   * @since 1.4.0
   */
  @Nullable
  public abstract String getSchemaUrl();

  /**
   * Returns a map of attributes that describe the resource, not associated with entities.
   *
   * @return a map of attributes.
   */
  final Attributes getUnassociatedAttributes() {
    AttributesBuilder unassociatedAttributes = getAttributes().toBuilder();
    unassociatedAttributes.removeIf(key -> EntityUtil.hasAttributeKey(getEntities(), key));
    return unassociatedAttributes.build();
  }

  /**
   * Returns a collection of associated entities.
   *
   * @return a collection of entities.
   */
  abstract Collection<Entity> getEntities();

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getAttributes();

  /**
   * Returns the value for a given resource attribute key.
   *
   * @return the value of the attribute with the given key
   */
  @Nullable
  public <T> T getAttribute(AttributeKey<T> key) {
    return getAttributes().get(key);
  }

  /**
   * Returns a new, merged {@link Resource} by merging the current {@code Resource} with the {@code
   * other} {@code Resource}. In case of a collision, the "other" {@code Resource} takes precedence.
   *
   * <p>Entity associations follow the rules documented in {@link ResourceBuilder}. Incoming
   * entities are processed before incoming unassociated attributes. Merging the same resources in
   * the same order can produce different entity associations depending on which pair is merged
   * first: {@code a.merge(b).merge(c)} can differ from {@code a.merge(b.merge(c))}, even if both
   * results have the same attribute keys and values.
   *
   * <p>The merged {@code schemaUrl} is computed from the two resources' {@code schemaUrl} values,
   * not from their entities. If either is null, the other is used. If both are non-null and differ,
   * the merged resource has no {@code schemaUrl}.
   *
   * @param other the {@code Resource} that will be merged with {@code this}.
   * @return the newly merged {@code Resource}.
   */
  public Resource merge(@Nullable Resource other) {
    if (other == null || other.equals(EMPTY)) {
      return this;
    }
    return builder()
        .putAll(this)
        .putAll(other)
        .buildWithSchemaUrl(mergeSchemaUrl(getSchemaUrl(), other.getSchemaUrl()));
  }

  @Nullable
  private static String mergeSchemaUrl(@Nullable String base, @Nullable String next) {
    if (base == null || next == null || base.equals(next)) {
      return base == null ? next : base;
    }
    logger.info(
        "Attempting to merge Resources with different schemaUrls. "
            + "The resulting Resource will have no schemaUrl assigned. Schema 1: "
            + base
            + " Schema 2: "
            + next);
    // currently, behavior is undefined if schema URLs don't match. In the future, we may
    // apply schema transformations if possible.
    return null;
  }

  /**
   * Returns a new {@link ResourceBuilder} instance for creating arbitrary {@link Resource}.
   *
   * @since 1.1.0
   */
  public static ResourceBuilder builder() {
    return new ResourceBuilder();
  }

  /**
   * Returns a new {@link ResourceBuilder} instance populated with the data of this {@link
   * Resource}.
   *
   * <p>A non-null schema URL is copied as explicit builder configuration, even if originally
   * derived from entities. Subsequent entity changes do not cause this URL to be re-derived.
   *
   * @since 1.1.0
   */
  public ResourceBuilder toBuilder() {
    ResourceBuilder resourceBuilder = builder().putAll(this);

    if (this.getSchemaUrl() != null) {
      resourceBuilder.setSchemaUrl(this.getSchemaUrl());
    }

    return resourceBuilder;
  }

  Resource() {}
}
