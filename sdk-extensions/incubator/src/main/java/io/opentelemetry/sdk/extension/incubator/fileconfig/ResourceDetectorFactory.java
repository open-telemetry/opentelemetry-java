/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNullResource;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorPropertyModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;

final class ResourceDetectorFactory
    implements Factory<ExperimentalResourceDetectorModel, Resource> {
  private static final String RESOURCE_NAME = "resource detector";

  private static final ResourceDetectorFactory INSTANCE = new ResourceDetectorFactory();

  private ResourceDetectorFactory() {}

  static ResourceDetectorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Resource create(
      ExperimentalResourceDetectorModel model, DeclarativeConfigContext context) {
    String key = null;
    Object value = null;

    if (model.getContainer() != null) {
      key = "container";
      value = model.getContainer();
    }
    if (model.getHost() != null) {
      requireNullResource(value, RESOURCE_NAME, model.getAdditionalProperties());
      key = "host";
      value = model.getHost();
    }
    if (model.getProcess() != null) {
      requireNullResource(value, RESOURCE_NAME, model.getAdditionalProperties());
      key = "process";
      value = model.getProcess();
    }
    if (model.getService() != null) {
      requireNullResource(value, RESOURCE_NAME, model.getAdditionalProperties());
      key = "service";
      value = model.getService();
    }
    if (key == null || value == null) {
      Map.Entry<String, ExperimentalResourceDetectorPropertyModel> keyValue =
          FileConfigUtil.getSingletonMapEntry(model.getAdditionalProperties(), "resource detector");
      key = keyValue.getKey();
      value = keyValue.getValue();
    }

    return context.loadComponent(Resource.class, key, value);
  }
}
