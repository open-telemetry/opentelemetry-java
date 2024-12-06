/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * A builder of {@link Resource} that allows to add key-value pairs and copy attributes from other
 * {@link Attributes} or {@link Resource}.
 *
 * @since 1.1.0
 */
public class ResourceBuilder {

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
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public <T> ResourceBuilder put(AttributeKey<T> key, T value) {
    if (key != null && key.getKey() != null && !key.getKey().isEmpty() && value != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public ResourceBuilder put(AttributeKey<Long> key, int value) {
    if (key != null && key.getKey() != null && !key.getKey().isEmpty()) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /** Puts all {@link Attributes} into this. */
  public ResourceBuilder putAll(Attributes attributes) {
    if (attributes != null) {
      attributesBuilder.putAll(attributes);
    }
    return this;
  }

  /** Puts all attributes + entities from {@link Resource} into this. */
  public ResourceBuilder putAll(Resource resource) {
    if (resource != null) {
      attributesBuilder.putAll(resource.getRawAttributes());
      addAll(new ArrayList<Entity>(resource.getEntities()));
    }
    return this;
  }

  /** Remove all attributes that satisfy the given predicate from {@link Resource}. */
  public ResourceBuilder removeIf(Predicate<AttributeKey<?>> filter) {
    // Remove all raw attributes.
    attributesBuilder.removeIf(filter);
    // Handle entities.
    if (entities.isEmpty()) {
      ArrayList<Entity> toAdd = new ArrayList<>();
      ArrayList<Entity> toRemove = new ArrayList<>();
      // If the predicate matches an identifying attribute we need to drop the entitiy.
      for (Entity e : entities) {
        if (e.getIdentifyingAttributes().asMap().keySet().stream().anyMatch(filter)) {
          // We must remove the entity.
          // We move all attributes into the "raw" section.
          toRemove.add(e);
          attributesBuilder.putAll(
              e.getIdentifyingAttributes().toBuilder().removeIf(filter).build());
          attributesBuilder.putAll(e.getAttributes().toBuilder().removeIf(filter).build());
        } else if (e.getAttributes().asMap().keySet().stream().anyMatch(filter)) {
          // We need to update the entity to remove the descirptive attribute.
          Entity newEntity = e.toBuilder().withDescriptive(d -> d.removeIf(filter)).build();
          toRemove.add(e);
          toAdd.add(newEntity);
        }
      }
      entities.removeAll(toRemove);
      entities.addAll(toAdd);
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

  public ResourceBuilder add(Entity e) {
    this.entities.add(e);
    // When adding an entity, we remove any raw attributes it may conflict with.
    this.attributesBuilder.removeIf(
        key ->
            entities.stream()
                .anyMatch(
                    entity ->
                        entity.getAttributes().asMap().containsKey(key)
                            || entity.getIdentifyingAttributes().asMap().containsKey(key)));
    return this;
  }

  public ResourceBuilder addAll(Collection<Entity> entities) {
    this.entities.addAll(entities);
    return this;
  }

  /** Create the {@link Resource} from this. */
  public Resource build() {
    // Derive schemaUrl from entitiy, if able.
    if (schemaUrl == null) {
      Set<String> entitySchemas =
          entities.stream().map(Entity::getSchemaUrl).collect(Collectors.toSet());
      if (entitySchemas.size() == 1) {
        // Updated Entities use same schema, we can preserve it.
        schemaUrl = entitySchemas.iterator().next();
      }
    }

    return Resource.create(attributesBuilder.build(), schemaUrl, entities);
  }
}
