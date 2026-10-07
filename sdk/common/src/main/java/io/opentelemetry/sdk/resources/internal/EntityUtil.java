/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Helper class for dealing with Entities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class EntityUtil {

  private EntityUtil() {}

  /**
   * Constructs a new {@link Resource} with Entity support.
   *
   * @param entities The set of entities the resource needs.
   * @return A constructed resource.
   */
  public static Resource createResource(Collection<Entity> entities) {
    ResourceBuilder builder = Resource.builder();
    entities.forEach(entity -> addEntity(builder, entity));
    return builder.build();
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

  /** Returns true if the attributes contain the given attribute key name, regardless of type. */
  public static boolean hasAttributeKey(Attributes attributes, String key) {
    return attributes.asMap().keySet().stream().anyMatch(existing -> existing.getKey().equals(key));
  }

  /** Returns true if the attributes share any attribute key name, regardless of type. */
  public static boolean sharesAttributeKey(Attributes first, Attributes second) {
    return first.asMap().keySet().stream().anyMatch(key -> hasAttributeKey(second, key.getKey()));
  }

  /** Returns true if the entities share any attribute key name, regardless of type. */
  public static boolean sharesAttributeKey(Entity first, Entity second) {
    return sharesAttributeKey(first.getId(), second.getId())
        || sharesAttributeKey(first.getId(), second.getDescription())
        || sharesAttributeKey(first.getDescription(), second.getId())
        || sharesAttributeKey(first.getDescription(), second.getDescription());
  }

  /** Returns null for either representation of an absent schema URL. */
  @Nullable
  static String normalizeSchemaUrl(@Nullable String schemaUrl) {
    return schemaUrl == null || schemaUrl.isEmpty() ? null : schemaUrl;
  }
}
