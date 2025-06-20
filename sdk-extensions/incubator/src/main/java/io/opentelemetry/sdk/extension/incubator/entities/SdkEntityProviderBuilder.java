/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.extension.incubator.entities.detectors.ServiceDetector;
import io.opentelemetry.sdk.extension.incubator.entities.detectors.TelemetrySdkDetector;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class SdkEntityProviderBuilder implements EntityProviderBuilder {
  private final List<EntityDetector> entityDetectors = new ArrayList<>();
  private final List<Resource> detectedResources = new ArrayList<>();
  private boolean includeDefaults = true;

  @Override
  public EntityProviderBuilder addDetector(EntityDetector detector) {
    this.entityDetectors.add(detector);
    return this;
  }

  @Override
  @Deprecated
  public EntityProviderBuilder addDetectedResource(Resource resource) {
    this.detectedResources.add(resource);
    return this;
  }

  private final Resource mergeDetectedAndRaw() {
    if (includeDefaults) {
      entityDetectors.add(new ServiceDetector());
      entityDetectors.add(new TelemetrySdkDetector());
    }
    Resource result = Resource.empty();
    for (EntityDetector detector : entityDetectors) {
      result =
          result.merge(
              EntityUtil.addAllEntity(
                      Resource.builder(),
                      detector.detect().stream()
                          .map(e -> ((PassthroughEntity) e).getPassthrough())
                          .collect(Collectors.toList()))
                  .build());
    }
    for (Resource next : detectedResources) {
      result = result.merge(next);
    }
    return result;
  }

  @Override
  public EntityProvider build() {
    return new SdkEntityProvider(mergeDetectedAndRaw());
  }

  @Override
  public EntityProviderBuilder includeDefaults(boolean include) {
    this.includeDefaults = include;
    return this;
  }
}
