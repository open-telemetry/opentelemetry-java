/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Helper class for dealing with Entities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class EntityUtil {
  private static final Logger logger = Logger.getLogger(EntityUtil.class.getName());

  private EntityUtil() {}

  /**
   * Constructs a new {@link Resource} with Entity support.
   *
   * @param entities The set of entities the resource needs.
   * @return A constructed resource.
   */
  public static Resource createResource(Collection<Entity> entities) {
    return createResourceRaw(
        Attributes.empty(), EntityUtil.mergeResourceSchemaUrl(entities, null, null), entities);
  }

  /**
   * Constructs a new {@link Resource} with Entity support.
   *
   * @param attributes The raw attributes for the resource.
   * @param schemaUrl The schema url for the resource.
   * @param entities The set of entities the resource needs.
   * @return A constructed resource.
   */
  static Resource createResourceRaw(
      Attributes attributes, @Nullable String schemaUrl, Collection<Entity> entities) {
    try {
      Method method =
          Resource.class.getDeclaredMethod(
              "create", Attributes.class, String.class, Collection.class);
      method.setAccessible(true);
      Object result = method.invoke(null, attributes, schemaUrl, entities);
      return (Resource) result;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Error calling create on Resource", e);
    }
  }

  /** Appends a new entity on to the end of the list of entities. */
  public static ResourceBuilder addEntity(ResourceBuilder rb, Entity e) {
    try {
      Method method = ResourceBuilder.class.getDeclaredMethod("addEntity", Entity.class);
      method.setAccessible(true);
      method.invoke(rb, e);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new IllegalStateException("Error calling addEntity on ResourceBuilder", ex);
    }
    return rb;
  }

  /**
   * Returns a collection of associated entities.
   *
   * @return a collection of entities.
   */
  @SuppressWarnings("unchecked")
  public static Collection<Entity> getEntities(Resource r) {
    try {
      Method method = Resource.class.getDeclaredMethod("getEntities");
      method.setAccessible(true);
      return (Collection<Entity>) method.invoke(r);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Error calling getEntities on Resource", e);
    }
  }

  /**
   * Returns a map of attributes that describe the resource, not associated with entities.
   *
   * @return a map of attributes.
   */
  public static Attributes getUnassociatedAttributes(Resource r) {
    try {
      Method method = Resource.class.getDeclaredMethod("getUnassociatedAttributes");
      method.setAccessible(true);
      return (Attributes) method.invoke(r);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException("Error calling getUnassociatedAttributes on Resource", e);
    }
  }

  /** Returns true if any entity in the collection has the attribute key, in id or description. */
  public static <T> boolean hasAttributeKey(Collection<Entity> entities, AttributeKey<T> key) {
    return entities.stream()
        .anyMatch(
            e -> e.getId().asMap().containsKey(key) || e.getDescription().asMap().containsKey(key));
  }

  /** Decides on a final SchemaURL for OTLP Resource based on entities chosen. */
  @Nullable
  static String mergeResourceSchemaUrl(
      Collection<Entity> entities, @Nullable String baseUrl, @Nullable String nextUrl) {
    // Check if entities all share the same URL.
    Set<String> entitySchemas =
        entities.stream().map(Entity::getSchemaUrl).collect(Collectors.toSet());
    // If we have no entities, we preserve previous schema url behavior.
    String result = baseUrl;
    if (entitySchemas.size() == 1) {
      // Updated Entities use same schema, we can preserve it.
      result = entitySchemas.iterator().next();
    } else if (entitySchemas.size() > 1) {
      // Entities use different schemas, resource must treat this as no schema_url.
      result = null;
    }

    // If schema url of merging resource is null, we use our current result.
    if (nextUrl == null) {
      return result;
    }
    // When there are no entities, we use old schema url merge behavior
    if (result == null && entities.isEmpty()) {
      return nextUrl;
    }
    if (!nextUrl.equals(result)) {
      logger.info(
          "Attempting to merge Resources with different schemaUrls. "
              + "The resulting Resource will have no schemaUrl assigned. Schema 1: "
              + baseUrl
              + " Schema 2: "
              + nextUrl);
      return null;
    }
    return result;
  }

  /**
   * Merges "loose" attributes on resource, removing those which conflict with the set of entities.
   *
   * @param base loose attributes from base resource
   * @param additional additional attributes to add to the resource.
   * @param entities the set of entites on the resource.
   * @return the new set of raw attributes for Resource and the set of conflicting entities that
   *     MUST NOT be reported on OTLP resource.
   */
  @SuppressWarnings("unchecked")
  static RawAttributeMergeResult mergeRawAttributes(
      Attributes base, Attributes additional, Collection<Entity> entities) {
    AttributesBuilder result = base.toBuilder();
    // We know attribute conflicts were handled perviously on the resource, so
    // This needs to account for entity merge of new entities, and remove raw
    // attributes that would have been removed with new entities.
    result.removeIf(key -> hasAttributeKey(entities, key));
    // For every "raw" attribute on the other resource, we merge into the
    // resource, but check for entity conflicts from previous entities.
    List<Entity> conflicts = new ArrayList<>();
    if (!additional.isEmpty()) {
      additional.forEach(
          (key, value) -> {
            for (Entity e : entities) {
              if (e.getId().get(key) != null || e.getDescription().get(key) != null) {
                // Remove the entity and push all attributes as raw,
                // we have an override.
                conflicts.add(e);
                result.putAll(e.getId()).putAll(e.getDescription());
              }
            }
            result.put((AttributeKey<Object>) key, value);
          });
    }
    return RawAttributeMergeResult.create(result.build(), conflicts);
  }

  /**
   * Merges incoming entities in iteration order according to the resource data model.
   *
   * <ul>
   *   <li>Entities with equal type, identifying attributes, and schema URL merge their
   *       descriptions, with incoming values taking precedence. Two absent schema URLs are equal.
   *   <li>An incoming entity with the same type but different identifying attributes or schema URL
   *       replaces the old entity, discarding all of the old entity's attributes.
   *   <li>If an incoming entity overwrites an attribute of an entity with a different type, remove
   *       that old entity's association and retain its non-conflicting attributes as unassociated.
   * </ul>
   *
   * <p>For cross-type conflicts, this method first copies all attributes of the removed entity into
   * {@code unassociatedAttributes}. The caller must then use {@link #mergeRawAttributes} to exclude
   * keys owned by the surviving entities, ensuring their values take precedence.
   *
   * @param base the initial set of entities.
   * @param additional Additional entities to merge with base set.
   * @param unassociatedAttributes Receives attributes from invalidated entity associations.
   * @return A new set of entities with no duplicate types.
   */
  static Collection<Entity> mergeEntities(
      Collection<Entity> base,
      Collection<Entity> additional,
      AttributesBuilder unassociatedAttributes) {
    Map<String, Entity> entities = new LinkedHashMap<>();
    base.forEach(e -> entities.put(e.getType(), e));
    for (Entity incoming : additional) {
      Entity old = entities.remove(incoming.getType());
      Entity next = incoming;
      // Compatible entities merge descriptions, with incoming values winning.
      // Otherwise, replace the old entity and all its attributes.
      if (old != null
          && old.getId().equals(incoming.getId())
          && Objects.equals(old.getSchemaUrl(), incoming.getSchemaUrl())) {
        next =
            incoming.toBuilder()
                .setDescription(
                    Attributes.builder()
                        .putAll(old.getDescription())
                        .putAll(incoming.getDescription())
                        .build())
                .build();
      } else if (old != null) {
        logger.fine(
            "Replacing entity ["
                + old.getType()
                + "] because "
                + (old.getId().equals(incoming.getId())
                    ? "schema URLs differ."
                    : "identifying attributes differ."));
      }
      Attributes incomingAttributes =
          Attributes.builder().putAll(next.getId()).putAll(next.getDescription()).build();
      Iterator<Entity> iterator = entities.values().iterator();
      while (iterator.hasNext()) {
        Entity existing = iterator.next();
        if (existing.getId().asMap().keySet().stream()
                .anyMatch(incomingAttributes.asMap()::containsKey)
            || existing.getDescription().asMap().keySet().stream()
                .anyMatch(incomingAttributes.asMap()::containsKey)) {
          logger.fine(
              "Removing entity association ["
                  + existing.getType()
                  + "] because incoming entity ["
                  + next.getType()
                  + "] overwrites its attributes. Non-conflicting attributes are retained as unassociated.");
          // Preserve the removed entity's non-conflicting values as ordinary resource attributes.
          // mergeRawAttributes later discards copied values for keys still supplied by entities.
          unassociatedAttributes.putAll(existing.getId()).putAll(existing.getDescription());
          iterator.remove();
        }
      }
      entities.put(next.getType(), next);
    }
    return new ArrayList<>(entities.values());
  }

  /**
   * Returns a new, merged {@link Resource} by merging the {@code base} {@code Resource} with the
   * {@code next} {@code Resource}. In case of a collision, the "next" {@code Resource} takes
   * precedence.
   *
   * @param base the {@code Resource} into which we merge new values.
   * @param next the {@code Resource} that will be merged with {@code base}.
   * @return the newly merged {@code Resource}.
   */
  public static Resource merge(Resource base, @Nullable Resource next) {
    if (next == null || next.equals(Resource.empty())) {
      return base;
    }
    AttributesBuilder unassociatedAttributes = getUnassociatedAttributes(base).toBuilder();
    Collection<Entity> entities =
        EntityUtil.mergeEntities(getEntities(base), getEntities(next), unassociatedAttributes);
    RawAttributeMergeResult attributeResult =
        EntityUtil.mergeRawAttributes(
            unassociatedAttributes.build(), getUnassociatedAttributes(next), entities);
    // Remove entities that are conflicting with raw attributes, and therefore in an unknown state.
    entities.removeAll(attributeResult.getConflicts());
    // Now figure out schema url for overall resource.
    String schemaUrl =
        EntityUtil.mergeResourceSchemaUrl(entities, base.getSchemaUrl(), next.getSchemaUrl());
    return createResourceRaw(attributeResult.getAttributes(), schemaUrl, entities);
  }
}
