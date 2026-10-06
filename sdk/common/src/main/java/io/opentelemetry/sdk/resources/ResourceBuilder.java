/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A builder for {@link Resource} that allows adding key-value pairs and copying attributes from
 * other {@link Attributes} or {@link Resource} instances.
 *
 * <p>Resources may carry experimental entity associations. Merge compatibility requires matching
 * entity types, identifying attributes, and schema URLs. Null and empty schema URLs both mean
 * absence. Identifying and descriptive attribute keys must be disjoint. Builder operations follow
 * these rules:
 *
 * <ul>
 *   <li>Later writes win, whether attributes are ordinary or entity-owned. Writing an unchanged
 *       stored type and value preserves association.
 *   <li>Compatible incoming entities merge descriptions. Different identifying attributes or
 *       schemas with the same type replace the whole old entity, including all its attributes.
 *   <li>An ordinary write that changes any entity-owned attribute, or an incoming entity that
 *       claims attributes from a different entity type, removes the affected association. All
 *       non-conflicting attributes remain as ordinary attributes, including identifying and
 *       descriptive attributes.
 *   <li>Filtering applies to all attributes and removes only selected attribute keys. Removing any
 *       identifying attribute key removes association and retains surviving attributes as ordinary
 *       attributes. Removing only descriptive attributes preserves association.
 * </ul>
 *
 * <p>Resource copying processes incoming entities before incoming ordinary attributes; regrouping
 * resource merges can change associations. Copying with {@link #putAll(Resource)} does not merge
 * resource schema URLs. {@link Resource#toBuilder()} copies a non-null source schema URL as
 * explicit configuration, even if it was derived. Without explicit configuration, each build
 * derives a common URL from the current entities. Pending ordinary attributes are validated at
 * {@link #build()}, not entity insertion.
 *
 * <p>These rules describe resource APIs, not environment parsing. Java {@link
 * Object#equals(Object)} is structural equality, not semantic resource identity; descriptions and
 * entity order affect resource equality.
 *
 * <p>Entity changes and association removals are logged at {@code INFO}. Unchanged operations do
 * not produce entity-change diagnostics.
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

  /** Puts all attributes from {@link Resource} into this. */
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
      Entity filtered = EntityUtil.removeAttributes(iterator.next(), filter, attributesBuilder);
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
    String resourceSchemaUrl = schemaUrl;
    if (resourceSchemaUrl == null) {
      resourceSchemaUrl = EntityUtil.mergeResourceSchemaUrl(entities, null, null);
    }

    return Resource.create(attributesBuilder.build(), resourceSchemaUrl, entities);
  }

  /** Merges an incoming entity without validating pending ordinary attributes. */
  ResourceBuilder addEntity(Entity e) {
    Collection<Entity> merged =
        EntityUtil.mergeEntities(entities, Collections.singletonList(e), attributesBuilder);
    attributesBuilder.removeIf(key -> EntityUtil.hasAttributeKey(merged, key));
    entities.clear();
    entities.addAll(merged);
    return this;
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
      if (EntityUtil.hasAttributeOverride(entity, overrides)) {
        logger.info(
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
}
