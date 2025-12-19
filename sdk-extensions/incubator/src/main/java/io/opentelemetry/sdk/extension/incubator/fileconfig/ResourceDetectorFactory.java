/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.LinkedHashMap;
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
      ExperimentalResourceDetectorModel model, DeclarativeConfigContext context) {
    Map<String, Object> detectorResourceByName = new LinkedHashMap<>();

    if (model.getContainer() != null) {
      detectorResourceByName.put("container", model.getContainer());
    }
    if (model.getHost() != null) {
      detectorResourceByName.put("host", model.getHost());
    }
    if (model.getProcess() != null) {
      detectorResourceByName.put("process", model.getProcess());
    }
    if (model.getService() != null) {
      detectorResourceByName.put("service", model.getService());
    }
    detectorResourceByName.putAll(model.getAdditionalProperties());

    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(detectorResourceByName, "resource detector");
    return context.loadComponent(Resource.class, keyValue.getKey(), keyValue.getValue());
  }
}
