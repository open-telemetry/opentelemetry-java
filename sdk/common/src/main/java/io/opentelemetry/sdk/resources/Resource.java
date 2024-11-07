/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.resources.detectors.ServiceDetector;
import io.opentelemetry.sdk.resources.detectors.TelemetrySdkDetector;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final Logger logger = Logger.getLogger(Resource.class.getName());
  private static final int MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";
  private static final Resource EMPTY = create(Attributes.empty());
  private static final Resource DEFAULT;

  static {
    // Update previous default to use new resource provider interface.
    DEFAULT =
        ResourceProvider.builder()
            .addEntityDetector(TelemetrySdkDetector.INSTANCE)
            .addEntityDetector(ServiceDetector.INSTANCE)
            .build()
            .getResource();
  }

  /**
   * Returns the default {@link Resource}. This resource contains the default attributes provided by
   * the SDK.
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
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   */
  public static Resource create(Attributes attributes) {
    return create(attributes, null, Collections.emptyList());
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of {@link Attributes} that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   */
  public static Resource create(Attributes attributes, @Nullable String schemaUrl) {
    checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    return new AutoValue_Resource(schemaUrl, attributes, Collections.emptyList());
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of {@link Attributes} that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   */
  public static Resource create(
      Attributes attributes, @Nullable String schemaUrl, Collection<Entity> entities) {
    checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    return new AutoValue_Resource(schemaUrl, attributes, entities);
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
   * Returns a map of attributes that describe the resource, not associated with entites.
   *
   * @return a map of attributes.
   */
  abstract Attributes getRawAttributes();

  /**
   * Returns a collectoion of associated entities.
   *
   * @return a collection of entities.
   */
  public abstract Collection<Entity> getEntities();

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   */
  // TODO - making this final breaks binary compatibility checks.
  public Attributes getAttributes() {
    // TODO - cache this.
    AttributesBuilder result = Attributes.builder();
    getEntities()
        .forEach(
            e -> {
              result.putAll(e.getIdentifyingAttributes());
              result.putAll(e.getAttributes());
            });
    // In merge rules, raw comes last, so we return these last.
    result.putAll(getRawAttributes());
    return result.build();
  }

  /**
   * Returns the value for a given resource attribute key.
   *
   * @return the value of the attribute with the given key
   */
  @Nullable
  public <T> T getAttribute(AttributeKey<T> key) {
    return getAttributes().get(key);
  }

  private static final Collection<Entity> mergeEntities(
      Collection<Entity> lhs, Collection<Entity> rhs) {
    if (lhs.isEmpty()) {
      return rhs;
    }
    if (rhs.isEmpty()) {
      return lhs;
    }
    Map<String, Entity> entities = new HashMap<>();
    lhs.forEach(e -> entities.put(e.getType(), e));
    for (Entity e : rhs) {
      if (!entities.containsKey(e.getType())) {
        entities.put(e.getType(), e);
      } else {
        Entity old = entities.get(e.getType());
        // If the entity identity is the same, but schema_url is different: drop the new entity d'
        // Note: We could offer configuration in this case
        if (old.getSchemaUrl() == null || !old.getSchemaUrl().equals(e.getSchemaUrl())) {
          logger.info(
              "Discovered conflicting entities. Entity ["
                  + old.getType()
                  + "] has different schema url ["
                  + old.getSchemaUrl()
                  + "], new entity  with schema url["
                  + e.getSchemaUrl()
                  + "] is dropped.");
        } else if (!old.getIdentifyingAttributes().equals(e.getIdentifyingAttributes())) {
          // If the entity identity is different: drop the new entity d'.
          logger.info(
              "Discovered conflicting entities. Entity ["
                  + old.getType()
                  + "] has identity ["
                  + old.getIdentifyingAttributes()
                  + "], new entity ["
                  + e.getIdentifyingAttributes()
                  + "] is dropped.");
        } else {
          // If the entity identiy and schema_url are the same, merge the descriptive attributes
          // of d' into e':
          //   For each descriptive attribute da' in d'
          //     If da'.key does not exist in e', then add da' to ei
          //     otherwise, ignore.
          Entity next =
              old.toBuilder()
                  .withDescriptive(
                      builder -> {
                        // Clean existing attributes.
                        builder.removeIf(ignore -> true);
                        // For attributes, last one wins.
                        // To ensure the previous attributes override,
                        // we write them second.
                        builder.putAll(e.getAttributes());
                        builder.putAll(old.getAttributes());
                      })
                  .build();
          entities.put(next.getType(), next);
        }
      }
    }
    return entities.values();
  }

  /**
   * Returns a new, merged {@link Resource} by merging the current {@code Resource} with the {@code
   * other} {@code Resource}. In case of a collision, the "other" {@code Resource} takes precedence.
   *
   * @param other the {@code Resource} that will be merged with {@code this}.
   * @return the newly merged {@code Resource}.
   */
  public Resource merge(@Nullable Resource other) {
    if (other == null || other == EMPTY) {
      return this;
    }

    // Merge Algorithm from
    // https://github.com/open-telemetry/oteps/blob/main/text/entities/0264-resource-and-entities.md#entity-merging-and-resource
    // A few caveats:
    //

    // First merge entities.
    Collection<Entity> entities = mergeEntities(getEntities(), other.getEntities());
    // Now perform merge logic, but ignore attributes from entities.

    // TODO - Warn on conflicts.
    AttributesBuilder attrBuilder = Attributes.builder();
    attrBuilder.putAll(this.getRawAttributes());
    // We know attribute conflicts were handled perviously on the resource, so
    // This needs to account for entity merge of new entities, and remove raw
    // attributes that would have been removed with new entities.
    attrBuilder.removeIf(
        key ->
            entities.stream()
                .anyMatch(
                    e ->
                        e.getIdentifyingAttributes().asMap().containsKey(key)
                            || e.getAttributes().asMap().containsKey(key)));

    // TODO - we need to update/identify conflicts in these with selected entities.
    // This may cause us to drop entities.
    attrBuilder.putAll(other.getRawAttributes());

    // Check if entities all share the same URL.
    Set<String> entitySchemas =
        entities.stream().map(Entity::getSchemaUrl).collect(Collectors.toSet());
    // If we have no entities, we preserve previous schema url behavior.
    String schemaUrl = getSchemaUrl();
    if (entitySchemas.size() == 1) {
      // Updated Entities use same schema, we can preserve it.
      schemaUrl = entitySchemas.iterator().next();
    } else if (entitySchemas.size() > 1) {
      // Entities use different schemas, resource must treat this as no schema_url.
      schemaUrl = null;
    }

    if (other.getSchemaUrl() == null) {
      return create(attrBuilder.build(), schemaUrl, entities);
    }
    // We fall back to old behavior here when entities aren't in the mix.
    if (schemaUrl == null && getEntities().isEmpty()) {
      return create(attrBuilder.build(), other.getSchemaUrl(), entities);
    }
    if (!other.getSchemaUrl().equals(schemaUrl)) {
      logger.info(
          "Attempting to merge Resources with different schemaUrls. "
              + "The resulting Resource will have no schemaUrl assigned. Schema 1: "
              + getSchemaUrl()
              + " Schema 2: "
              + other.getSchemaUrl());
      // currently, behavior is undefined if schema URLs don't match. In the future, we may
      // apply schema transformations if possible.
      return create(attrBuilder.build(), null, entities);
    }
    return create(attrBuilder.build(), schemaUrl, entities);
  }

  private static void checkAttributes(Attributes attributes) {
    attributes.forEach(
        (key, value) -> {
          Utils.checkArgument(
              isValidAndNotEmpty(key), "Attribute key" + ERROR_MESSAGE_INVALID_CHARS);
          Objects.requireNonNull(value, "Attribute value" + ERROR_MESSAGE_INVALID_VALUE);
        });
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length not
   * exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValid(String name) {
    return name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length
   * greater than 0 and not exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValidAndNotEmpty(AttributeKey<?> name) {
    return !name.getKey().isEmpty() && isValid(name.getKey());
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
   * @since 1.1.0
   */
  public ResourceBuilder toBuilder() {
    ResourceBuilder resourceBuilder = builder().putAll(this);

    if (this.getSchemaUrl() != null) {
      resourceBuilder.setSchemaUrl(this.getSchemaUrl());
    }

    return resourceBuilder;
  }

  // TODO - Should we override toString to previous behavior?

  Resource() {}
}
