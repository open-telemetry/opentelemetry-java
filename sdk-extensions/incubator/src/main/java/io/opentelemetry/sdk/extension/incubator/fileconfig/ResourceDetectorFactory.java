/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.resources.Resource;
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
    Map.Entry<String, Object> keyValue =
        FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "resource detector");
    return context.loadComponent(Resource.class, keyValue.getKey(), keyValue.getValue(), context);
  }
}
