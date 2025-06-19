/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
                      builder -> {
                        // Clean existing attributes.
                        builder.removeIf(ignore -> true);
                        // For attributes, last one wins.
                        // To ensure the previous attributes override,
                        // we write them second.
                        builder.putAll(e.getDescription());
                        builder.putAll(old.getDescription());
                      })
                  .build();
          entities.put(next.getType(), next);
        }
      }
    }
    return entities.values();
  }
}
