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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
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
  public static final Resource createResource(Collection<Entity> entities) {
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
  static final Resource createResourceRaw(
      Attributes attributes, @Nullable String schemaUrl, Collection<Entity> entities) {
    try {
      Method method =
          Resource.class.getDeclaredMethod(
              "create", Attributes.class, String.class, Collection.class);
      if (method != null) {
        method.setAccessible(true);
        Object result = method.invoke(null, attributes, schemaUrl, entities);
        if (result instanceof Resource) {
          return (Resource) result;
        }
      }
    } catch (NoSuchMethodException nme) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", nme);
    } catch (IllegalAccessException iae) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", iae);
    } catch (InvocationTargetException ite) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", ite);
    }
    // Fall back to non-entity behavior?
    logger.log(Level.WARNING, "Attempting to use entities with unsupported resource");
    return Resource.empty();
  }

  /** Appends a new entity on to the end of the list of entities. */
  public static final ResourceBuilder addEntity(ResourceBuilder rb, Entity e) {
    try {
      Method method = ResourceBuilder.class.getDeclaredMethod("add", Entity.class);
      if (method != null) {
        method.setAccessible(true);
        method.invoke(rb, e);
      }
    } catch (NoSuchMethodException nme) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", nme);
    } catch (IllegalAccessException iae) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", iae);
    } catch (InvocationTargetException ite) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", ite);
    }
    return rb;
  }

  /** Appends a new collection of entities on to the end of the list of entities. */
  public static final ResourceBuilder addAllEntity(ResourceBuilder rb, Collection<Entity> e) {
    try {
      Method method = ResourceBuilder.class.getDeclaredMethod("addAll", Collection.class);
      if (method != null) {
        method.setAccessible(true);
        method.invoke(rb, e);
      }
    } catch (NoSuchMethodException nme) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", nme);
    } catch (IllegalAccessException iae) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", iae);
    } catch (InvocationTargetException ite) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", ite);
    }
    return rb;
  }

  /**
   * Returns a collectoion of associated entities.
   *
   * @return a collection of entities.
   */
  @SuppressWarnings("unchecked")
  public static final Collection<Entity> getEntities(Resource r) {
    try {
      Method method = Resource.class.getDeclaredMethod("getEntities");
      if (method != null) {
        method.setAccessible(true);
        return (Collection<Entity>) method.invoke(r);
      }
    } catch (NoSuchMethodException nme) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", nme);
    } catch (IllegalAccessException iae) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", iae);
    } catch (InvocationTargetException ite) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", ite);
    }
    return Collections.emptyList();
  }

  /**
   * Returns a map of attributes that describe the resource, not associated with entites.
   *
   * @return a map of attributes.
   */
  public static final Attributes getRawAttributes(Resource r) {
    try {
      Method method = Resource.class.getDeclaredMethod("getRawAttributes");
      if (method != null) {
        method.setAccessible(true);
        return (Attributes) method.invoke(r);
      }
    } catch (NoSuchMethodException nme) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", nme);
    } catch (IllegalAccessException iae) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", iae);
    } catch (InvocationTargetException ite) {
      logger.log(Level.WARNING, "Attempting to use entities with unsupported resource", ite);
    }
    return Attributes.empty();
  }

  /** Returns true if any entity in the collection has the attribute key, in id or description. */
  public static final <T> boolean hasAttributeKey(
      Collection<Entity> entities, AttributeKey<T> key) {
    return entities.stream()
        .anyMatch(
            e -> e.getId().asMap().containsKey(key) || e.getDescription().asMap().containsKey(key));
  }

  /** Decides on a final SchemaURL for OTLP Resource based on entities chosen. */
  @Nullable
  static final String mergeResourceSchemaUrl(
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
  static final RawAttributeMergeResult mergeRawAttributes(
      Attributes base, Attributes additional, Collection<Entity> entities) {
    AttributesBuilder result = base.toBuilder();
    // We know attribute conflicts were handled perviously on the resource, so
    // This needs to account for entity merge of new entities, and remove raw
    // attributes that would have been removed with new entities.
    result.removeIf(key -> hasAttributeKey(entities, key));
    // For every "raw" attribute on the other resource, we merge into the
    // resource, but check for entity conflicts from previous entities.
    ArrayList<Entity> conflicts = new ArrayList<>();
    if (!additional.isEmpty()) {
      additional.forEach(
          (key, value) -> {
            for (Entity e : entities) {
              if (e.getId().asMap().keySet().contains(key)
                  || e.getDescription().asMap().keySet().contains(key)) {
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
   * Merges entities according to specification rules.
   *
   * @param base the initial set of entities.
   * @param additional Additional entities to merge with base set.
   * @return A new set of entities with no duplicate types.
   */
  public static final Collection<Entity> mergeEntities(
      Collection<Entity> base, Collection<Entity> additional) {
    if (base.isEmpty()) {
      return additional;
    }
    if (additional.isEmpty()) {
      return base;
    }
    Map<String, Entity> entities = new HashMap<>();
    base.forEach(e -> entities.put(e.getType(), e));
    for (Entity e : additional) {
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
        } else if (!old.getId().equals(e.getId())) {
          // If the entity identity is different: drop the new entity d'.
          logger.info(
              "Discovered conflicting entities. Entity ["
                  + old.getType()
                  + "] has identity ["
                  + old.getId()
                  + "], new entity ["
                  + e.getId()
                  + "] is dropped.");
        } else {
          // If the entity identity and schema_url are the same, merge the descriptive attributes
          // of d' into e':
          //   For each descriptive attribute da' in d'
          //     If da'.key does not exist in e', then add da' to ei
          //     otherwise, ignore.
          Entity next =
              old.toBuilder()
                  .withDescription(
                      Attributes.builder()
                          .putAll(e.getDescription())
                          .putAll(old.getDescription())
                          .build())
                  .build();
          entities.put(next.getType(), next);
        }
      }
    }
    return entities.values();
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
    if (next == null || next == Resource.empty()) {
      return base;
    }
    // Merge Algorithm from
    // https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/entities/0264-resource-and-entities.md#entity-merging-and-resource
    Collection<Entity> entities = EntityUtil.mergeEntities(getEntities(base), getEntities(next));
    RawAttributeMergeResult attributeResult =
        EntityUtil.mergeRawAttributes(getRawAttributes(base), getRawAttributes(next), entities);
    // Remove entiites that are conflicting with raw attributes, and therefore in an unknown state.
    entities.removeAll(attributeResult.getConflicts());
    // Now figure out schema url for overall resource.
    String schemaUrl =
        EntityUtil.mergeResourceSchemaUrl(entities, base.getSchemaUrl(), next.getSchemaUrl());
    return createResourceRaw(attributeResult.getAttributes(), schemaUrl, entities);
  }
}
