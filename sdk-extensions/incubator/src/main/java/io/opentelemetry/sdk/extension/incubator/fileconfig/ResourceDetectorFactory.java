/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.Map;

final class ResourceDetectorFactory
    implements Factory<ExperimentalResourceDetectorModel, Resource> {

  private static final ResourceDetectorFactory INSTANCE = new ResourceDetectorFactory();

  private ResourceDetectorFactory() {}

  static ResourceDetectorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Resource create(
      ExperimentalResourceDetectorModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    Map<String, Object> additionalProperties = model.getAdditionalProperties();
    if (!additionalProperties.isEmpty()) {
      if (additionalProperties.size() > 1) {
        throw new DeclarativeConfigException(
            "Invalid configuration - multiple resource detectors set: "
                + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
      }
      Map.Entry<String, Object> detectorKeyValue =
          additionalProperties.entrySet().stream()
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException("Missing detector. This is a programming error."));
      Resource resource =
          FileConfigUtil.loadComponent(
              spiHelper, Resource.class, detectorKeyValue.getKey(), detectorKeyValue.getValue());
      return resource;
    } else {
      throw new DeclarativeConfigException("resource detector must be set");
    }
  }
}
