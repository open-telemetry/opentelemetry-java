/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A builder for {@link Resource} that allows adding key-value pairs and copying attributes from
 * other {@link Attributes} or {@link Resource} instances.
 *
 * <p>Resources may carry experimental entity associations. Within an entity, identifying and
 * descriptive attribute keys must not overlap.
 *
 * <p>Resource-level {@code schemaUrl} handling is best effort: conflicts do not prevent attribute
 * merging, but leave the merged resource without a {@code schemaUrl}. Entity merge compatibility
 * requires matching types, identifying attributes, and entity {@code schemaUrl} values. For
 * entities, null and empty {@code schemaUrl} values both represent absence and match each other.
 * Conflicting entity {@code schemaUrl} values may indicate a configuration or version mismatch.
 *
 * <p>Builder operations follow these rules:
 *
 * <ul>
 *   <li>Later writes win, whether attributes are ordinary or entity-owned. Writing the same value
 *       with the same attribute type does not remove the entity association.
 *   <li>Compatible incoming entities merge descriptions. Different identifying attributes or {@code
 *       schemaUrl} values with the same type replace the whole old entity, including all its
 *       attributes.
 *   <li>An ordinary write that changes any entity-owned attribute, or an incoming entity that
 *       claims attributes from a different entity type, removes the affected entity association.
 *       All non-conflicting attributes remain as ordinary attributes.
 *   <li>Filtering applies to ordinary and entity-owned attributes. Removing any entity identifying
 *       attribute key removes the entity association and retains surviving attributes as ordinary
 *       attributes. Removing only descriptive attributes preserves entity association.
 * </ul>
 *
 * <p>Both {@link #putAll(Resource)} and {@link Resource#merge(Resource)} apply incoming entities
 * before incoming ordinary attributes. Merging the same resources in the same order can produce
 * different entity associations depending on which pair is merged first: {@code
 * a.merge(b).merge(c)} can differ from {@code a.merge(b.merge(c))}.
 *
 * <p>Without explicit configuration, each {@link #build()} call derives the {@code schemaUrl}
 * shared by all current entities that declare one, or null if none declare one or their values
 * differ. {@link #putAll(Resource)} does not copy {@code schemaUrl}. {@link Resource#toBuilder()}
 * copies a non-null source {@code schemaUrl} as explicit configuration, even if it was derived from
 * entities.
 *
 * <p>{@link #build()} rejects ordinary attributes whose key names are empty, contain characters
 * outside printable ASCII, or exceed 255 characters.
 *
 * <p>These rules describe resource APIs, not environment parsing. Java {@link
 * Object#equals(Object)} is structural equality, not semantic resource identity. Descriptions and
 * entity order affect resource equality.
 *
 * <p>Entity replacements, description changes, and association removals are logged at {@code
 * WARNING}.
 *
 * @since 1.1.0
 */
public class ResourceBuilder {

  private static final Logger logger = Logger.getLogger(ResourceBuilder.class.getName());

  private final AttributesBuilder attributesBuilder = Attributes.builder();
  private final List<Entity> entities = new ArrayList<>();
  @Nullable private String schemaUrl;

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, String value) {
    if (key != null && value != null) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a long attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, long value) {
    if (key != null) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a double attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, double value) {
    if (key != null) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a boolean attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, boolean value) {
    if (key != null) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, String... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, long... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, double... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
      removeEntityAssociations(key);
    }
    return this;
  }

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, boolean... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
      removeEntityAssociations(key);
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public <T> ResourceBuilder put(AttributeKey<T> key, T value) {
    if (key != null && key.getKey() != null && !key.getKey().isEmpty() && value != null) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key.getKey());
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public ResourceBuilder put(AttributeKey<Long> key, int value) {
    if (key != null && key.getKey() != null && !key.getKey().isEmpty()) {
      attributesBuilder.put(key, value);
      removeEntityAssociations(key.getKey());
    }
    return this;
  }

  /** Puts all {@link Attributes} into this. */
  public ResourceBuilder putAll(Attributes attributes) {
    if (attributes != null) {
      attributesBuilder.putAll(attributes);
      removeEntityAssociations();
    }
    return this;
  }

  /**
   * Copies entity associations and attributes from {@link Resource}, following the class-level
   * rules. Does not copy {@code schemaUrl}.
   */
  public ResourceBuilder putAll(Resource resource) {
    if (resource != null) {
      resource.getEntities().forEach(this::addEntity);
      putAll(resource.getUnassociatedAttributes());
    }
    return this;
  }

  /** Remove all attributes that satisfy the given predicate from {@link Resource}. */
  public ResourceBuilder removeIf(Predicate<AttributeKey<?>> filter) {
    if (filter == null) {
      return this;
    }
    attributesBuilder.removeIf(filter);
    ListIterator<Entity> iterator = entities.listIterator();
    while (iterator.hasNext()) {
      Entity filtered = removeAttributes(iterator.next(), filter);
      if (filtered == null) {
        iterator.remove();
      } else {
        iterator.set(filtered);
      }
    }
    return this;
  }

  /**
   * Assign an OpenTelemetry schema URL to the resulting Resource.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used to create this Resource.
   * @return this
   * @since 1.4.0
   */
  public ResourceBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /** Create the {@link Resource} from this. */
  public Resource build() {
    return buildWithSchemaUrl(schemaUrl != null ? schemaUrl : deriveSchemaUrl(entities));
  }

  /** Builds using {@code resourceSchemaUrl} as-is, without deriving it from entities. */
  Resource buildWithSchemaUrl(@Nullable String resourceSchemaUrl) {
    return Resource.create(attributesBuilder.build(), resourceSchemaUrl, entities);
  }

  /**
   * Merges an incoming entity without validating pending ordinary attributes.
   *
   * <p>An existing entity of the same type with equal identifying attributes and schema URL merges
   * descriptions, with incoming values winning. Otherwise, the incoming entity replaces it. Any
   * other entity sharing an attribute key with the incoming entity loses its association, and its
   * attributes are retained as unassociated unless the incoming entity owns them.
   */
  ResourceBuilder addEntity(Entity incoming) {
    Entity next = incoming;
    Entity old = removeEntity(incoming.getType());
    if (old != null) {
      if (old.getId().equals(incoming.getId())
          && Objects.equals(
              normalizeSchemaUrl(old.getSchemaUrl()),
              normalizeSchemaUrl(incoming.getSchemaUrl()))) {
        next =
            incoming.toBuilder()
                .setDescription(
                    Attributes.builder()
                        .putAll(old.getDescription())
                        .putAll(incoming.getDescription())
                        .build())
                .build();
        if (!next.getDescription().equals(old.getDescription())) {
          logger.warning("Updating descriptive attributes of entity [" + next.getType() + "].");
        }
      } else {
        logger.warning(
            "Replacing entity ["
                + old.getType()
                + "] because "
                + (old.getId().equals(incoming.getId())
                    ? "schema URLs differ."
                    : "identifying attributes differ."));
      }
    }
    Iterator<Entity> iterator = entities.iterator();
    while (iterator.hasNext()) {
      Entity existing = iterator.next();
      if (EntityUtil.sharesAttributeKey(existing, next)) {
        logger.warning(
            "Removing entity association ["
                + existing.getType()
                + "] because incoming entity ["
                + next.getType()
                + "] overwrites its attributes. Non-conflicting attributes are retained as unassociated.");
        attributesBuilder.putAll(existing.getId()).putAll(existing.getDescription());
        iterator.remove();
      }
    }
    entities.add(next);
    attributesBuilder.removeIf(key -> EntityUtil.hasAttributeKey(entities, key));
    return this;
  }

  @Nullable
  private Entity removeEntity(String type) {
    Iterator<Entity> iterator = entities.iterator();
    while (iterator.hasNext()) {
      Entity entity = iterator.next();
      if (entity.getType().equals(type)) {
        iterator.remove();
        return entity;
      }
    }
    return null;
  }

  /**
   * Removes matching attributes from the entity. Removing any identifying attribute removes the
   * association and retains the remaining attributes as unassociated.
   *
   * @return the filtered entity, or null if its association was removed.
   */
  @Nullable
  private Entity removeAttributes(Entity entity, Predicate<AttributeKey<?>> filter) {
    Attributes id = entity.getId().toBuilder().removeIf(filter).build();
    Attributes description = entity.getDescription().toBuilder().removeIf(filter).build();
    if (!id.equals(entity.getId())) {
      logger.warning(
          "Removing entity association ["
              + entity.getType()
              + "] because identifying attribute keys were filtered. Remaining attributes are retained as unassociated.");
      attributesBuilder.putAll(id).putAll(description);
      return null;
    }
    if (description.equals(entity.getDescription())) {
      return entity;
    }
    logger.warning("Removing descriptive attribute keys from entity [" + entity.getType() + "].");
    return entity.toBuilder().setDescription(description).build();
  }

  private void removeEntityAssociations(String key) {
    if (!entities.isEmpty() && EntityUtil.hasAttributeKey(entities, key)) {
      removeEntityAssociations();
    }
  }

  private void removeEntityAssociations() {
    if (entities.isEmpty()) {
      return;
    }
    Attributes overrides = attributesBuilder.build();
    boolean removed = false;
    Iterator<Entity> iterator = entities.iterator();
    while (iterator.hasNext()) {
      Entity entity = iterator.next();
      if (hasAttributeOverride(entity.getId(), overrides)
          || hasAttributeOverride(entity.getDescription(), overrides)) {
        logger.warning(
            "Removing entity association ["
                + entity.getType()
                + "] because resource attributes change the value or type of its attributes. Other attributes are retained as unassociated.");
        attributesBuilder.putAll(entity.getId()).putAll(entity.getDescription());
        iterator.remove();
        removed = true;
      }
    }
    if (removed) {
      attributesBuilder.putAll(overrides);
    }
    // Unchanged writes remain entity-owned rather than becoming loose attributes.
    attributesBuilder.removeIf(key -> EntityUtil.hasAttributeKey(entities, key));
  }

  /** Returns true if incoming attributes change the value or type of a current attribute. */
  private static boolean hasAttributeOverride(Attributes current, Attributes incoming) {
    // Map lookup compares attribute types without valueKey's type coercion.
    return incoming.asMap().entrySet().stream()
        .anyMatch(
            entry ->
                EntityUtil.hasAttributeKey(current, entry.getKey().getKey())
                    && !Objects.equals(current.asMap().get(entry.getKey()), entry.getValue()));
  }

  /** Returns the schema URL shared by all entities that declare one, or null if they differ. */
  @Nullable
  private static String deriveSchemaUrl(List<Entity> entities) {
    Set<String> schemaUrls = new HashSet<>();
    for (Entity entity : entities) {
      String entitySchemaUrl = normalizeSchemaUrl(entity.getSchemaUrl());
      if (entitySchemaUrl != null) {
        schemaUrls.add(entitySchemaUrl);
      }
    }
    return schemaUrls.size() == 1 ? schemaUrls.iterator().next() : null;
  }

  @Nullable
  static String normalizeSchemaUrl(@Nullable String schemaUrl) {
    return StringUtils.isNullOrEmpty(schemaUrl) ? null : schemaUrl;
  }
}
