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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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
    AttributesBuilder unassociatedAttributes = Attributes.builder();
    Collection<Entity> normalized =
        mergeEntities(Collections.emptyList(), entities, unassociatedAttributes);
    unassociatedAttributes.removeIf(key -> hasAttributeKey(normalized, key));
    return createResourceRaw(
        unassociatedAttributes.build(), mergeResourceSchemaUrl(normalized, null, null), normalized);
  }

  /**
   * Builds a resource after conflicts have been resolved. Each attribute key must have one owner.
   * This method rejects conflicts instead of resolving them; callers must apply ordinary attributes
   * first and entities second.
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

  /** Merges an entity into the builder, resolving identity and attribute key conflicts. */
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
    return hasAttributeKey(entities, key.getKey());
  }

  /** Returns true if any entity references an attribute with the given name. */
  public static boolean hasAttributeKey(Collection<Entity> entities, String key) {
    return entities.stream()
        .anyMatch(e -> hasAttributeKey(e.getId(), key) || hasAttributeKey(e.getDescription(), key));
  }

  private static boolean hasAttributeKey(Attributes attributes, String key) {
    return attributes.asMap().keySet().stream().anyMatch(existing -> existing.getKey().equals(key));
  }

  /** Returns true if incoming attributes change the value or type of an entity-owned attribute. */
  public static boolean hasAttributeOverride(Entity entity, Attributes incoming) {
    return hasAttributeOverride(entity.getId(), incoming)
        || hasAttributeOverride(entity.getDescription(), incoming);
  }

  private static boolean hasAttributeOverride(Attributes current, Attributes incoming) {
    // Map lookup compares attribute types without valueKey's type coercion.
    return incoming.asMap().entrySet().stream()
        .anyMatch(
            entry ->
                hasAttributeKey(current, entry.getKey().getKey())
                    && !Objects.equals(current.asMap().get(entry.getKey()), entry.getValue()));
  }

  /**
   * Removes matching attributes and invalidates the association if identifying attributes change.
   *
   * @param entity the entity to filter.
   * @param filter selects attributes to remove.
   * @param unassociatedAttributes receives remaining attributes when the association is removed.
   * @return the filtered entity, or null if its association was removed.
   */
  @Nullable
  public static Entity removeAttributes(
      Entity entity, Predicate<AttributeKey<?>> filter, AttributesBuilder unassociatedAttributes) {
    Attributes id = entity.getId().toBuilder().removeIf(filter).build();
    Attributes description = entity.getDescription().toBuilder().removeIf(filter).build();
    if (!id.equals(entity.getId())) {
      logger.info(
          "Removing entity association ["
              + entity.getType()
              + "] because identifying attribute keys were filtered. Remaining attributes are retained as unassociated.");
      unassociatedAttributes.putAll(id).putAll(description);
      return null;
    }
    if (!description.equals(entity.getDescription())) {
      logger.info("Removing descriptive attribute keys from entity [" + entity.getType() + "].");
    }
    return SdkEntity.create(entity.getType(), id, description, entity.getSchemaUrl());
  }

  /** Returns null for either representation of an absent schema URL. */
  @Nullable
  static String normalizeSchemaUrl(@Nullable String schemaUrl) {
    return schemaUrl == null || schemaUrl.isEmpty() ? null : schemaUrl;
  }

  /** Decides on a final SchemaURL for OTLP Resource based on entities chosen. */
  @Nullable
  public static String mergeResourceSchemaUrl(
      Collection<Entity> entities, @Nullable String baseUrl, @Nullable String nextUrl) {
    baseUrl = normalizeSchemaUrl(baseUrl);
    nextUrl = normalizeSchemaUrl(nextUrl);
    // Check if entities all share the same URL.
    Set<String> entitySchemas =
        entities.stream()
            .map(entity -> normalizeSchemaUrl(entity.getSchemaUrl()))
            .collect(Collectors.toSet());
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
  static RawAttributeMergeResult mergeRawAttributes(
      Attributes base, Attributes additional, Collection<Entity> entities) {
    AttributesBuilder result = base.toBuilder();
    result.removeIf(key -> hasAttributeKey(entities, key));
    List<Entity> conflicts = new ArrayList<>();
    for (Entity entity : entities) {
      if (hasAttributeOverride(entity, additional)) {
        logger.info(
            "Removing entity association ["
                + entity.getType()
                + "] because resource attributes change the value or type of its attributes. Other attributes are retained as unassociated.");
        conflicts.add(entity);
        result.putAll(entity.getId()).putAll(entity.getDescription());
      }
    }
    result.putAll(additional);
    List<Entity> remainingEntities = new ArrayList<>(entities);
    remainingEntities.removeAll(conflicts);
    result.removeIf(key -> hasAttributeKey(remainingEntities, key));
    return RawAttributeMergeResult.create(result.build(), conflicts);
  }

  /**
   * Merges incoming entities in iteration order according to the resource data model.
   *
   * <ul>
   *   <li>Entities with equal type, identifying attributes, and schema URL merge their
   *       descriptions, with incoming values taking precedence. Null and empty schema URLs both
   *       represent absence and are equal.
   *   <li>An incoming entity with the same type but different identifying attributes or schema URL
   *       replaces the old entity, discarding all of the old entity's attributes.
   *   <li>If an incoming entity overwrites an attribute of an entity with a different type, remove
   *       that old entity's association and retain its non-conflicting attributes as unassociated.
   * </ul>
   *
   * <p>For cross-type conflicts, this method first copies all attributes of the removed entity into
   * {@code unassociatedAttributes}. The caller must exclude keys owned by surviving entities,
   * ensuring their values take precedence. When merging incoming raw attributes, use {@link
   * #mergeRawAttributes} afterward.
   *
   * @param base the initial set of entities.
   * @param additional Additional entities to merge with base set.
   * @param unassociatedAttributes Receives attributes from invalidated entity associations.
   * @return A new set of entities with no duplicate types.
   */
  public static Collection<Entity> mergeEntities(
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
          logger.info("Updating descriptive attributes of entity [" + next.getType() + "].");
        }
      } else if (old != null) {
        logger.info(
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
                .anyMatch(key -> hasAttributeKey(incomingAttributes, key.getKey()))
            || existing.getDescription().asMap().keySet().stream()
                .anyMatch(key -> hasAttributeKey(incomingAttributes, key.getKey()))) {
          logger.info(
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
   * precedence. Incoming entities are processed before incoming unassociated attributes. Regrouping
   * merges can change entity associations even when flattened attributes are unchanged.
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
