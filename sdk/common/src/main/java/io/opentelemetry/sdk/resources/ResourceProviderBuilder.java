/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Builder of {@link ResourceProvider} */
public final class ResourceProviderBuilder {

  private final List<EntityDetector> entityDetectors = new ArrayList<>();
  private final List<Resource> detectedResources = new ArrayList<>();

  public ResourceProviderBuilder addEntityDetector(EntityDetector detector) {
    this.entityDetectors.add(detector);
    return this;
  }

  public ResourceProviderBuilder addDetectedResource(Resource resource) {
    this.detectedResources.add(resource);
    return this;
  }

  // TODO - we need this on exposed Resource.merge function...
  private Resource mergeEntities() {
    Map<String, Entity> entities = new HashMap<>();
    for (EntityDetector detector : entityDetectors) {
      for (Entity e : detector.detectEntities()) {
        if (!entities.containsKey(e.getType())) {
          entities.put(e.getType(), e);
        } else {
          Entity old = entities.get(e.getType());
          // If the entity identity is the same, but schema_url is different: drop the new entity d'
          // Note: We could offer configuration in this case
          if (old.getSchemaUrl() == null || old.getSchemaUrl().equals(e.getSchemaUrl())) {
            // If the entity identity is different: drop the new entity d'.
            if (old.getIdentifyingAttributes().equals(e.getIdentifyingAttributes())) {
              // If the entity identiy and schema_url are the same, merge the descriptive attributes
              // of d' into e':
              //   For each descriptive attribute da' in d'
              //     If da'.key does not exist in e', then add da' to ei
              //     otherwise, ignore.
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
                      });
            }
          }
        }
      }
    }
    // Now merge entities into resource.
    return Resource.builder().addAll(entities.values()).build();
  }

  private Resource mergeDetected() {
    Resource result = mergeEntities();
    for (Resource next : detectedResources) {
      result = result.merge(next);
    }
    return result;
  }

  public ResourceProvider build() {
    return new ResourceProvider(mergeDetected());
  }
}
