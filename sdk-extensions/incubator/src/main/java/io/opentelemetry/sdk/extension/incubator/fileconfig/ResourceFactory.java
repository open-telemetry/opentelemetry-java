/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.io.Closeable;
import java.util.List;

final class ResourceFactory implements Factory<Resource, io.opentelemetry.sdk.resources.Resource> {

  private static final ResourceFactory INSTANCE = new ResourceFactory();

  private ResourceFactory() {}

  static ResourceFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public io.opentelemetry.sdk.resources.Resource create(
      Resource model, SpiHelper spiHelper, List<Closeable> closeables) {
    ResourceBuilder builder = io.opentelemetry.sdk.resources.Resource.getDefault().toBuilder();

    Attributes attributesModel = model.getAttributes();
    if (attributesModel != null) {
      builder.putAll(
          AttributesFactory.getInstance().create(attributesModel, spiHelper, closeables));
    }

    return builder.build();
  }
}
