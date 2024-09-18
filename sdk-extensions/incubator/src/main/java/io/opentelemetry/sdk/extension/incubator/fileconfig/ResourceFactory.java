/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributesModel;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class ResourceFactory
    implements Factory<
        io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel,
        Resource> {

  private static final StructuredConfigProperties EMPTY_CONFIG =
      FileConfiguration.toConfigProperties(Collections.emptyMap());
  private static final ResourceFactory INSTANCE = new ResourceFactory();

  private ResourceFactory() {}

  static ResourceFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Resource create(
      io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    Resource result = Resource.getDefault();

    List<Resource> resourceDetectorResources = loadFromResourceDetectors(spiHelper);
    for (Resource resourceProviderResource : resourceDetectorResources) {
      result = result.merge(resourceProviderResource);
    }

    AttributesModel attributesModel = model.getAttributes();
    if (attributesModel != null) {
      result =
          result.toBuilder()
              .putAll(
                  AttributesFactory.getInstance().create(attributesModel, spiHelper, closeables))
              .build();
    }

    return result;
  }

  /**
   * Load resources from resource detectors, in order of lowest priority to highest priority.
   *
   * <p>In file configuration, a resource detector is a {@link ComponentProvider} with {@link
   * ComponentProvider#getType()} set to {@link Resource}. Unlike other {@link ComponentProvider}s,
   * the resource detector version does not use {@link ComponentProvider#getName()} (except for
   * debug messages), and {@link ComponentProvider#create(StructuredConfigProperties)} is called
   * with an empty instance. Additionally, the {@link Ordered#order()} value is respected for
   * resource detectors which implement {@link Ordered}.
   */
  @SuppressWarnings("rawtypes")
  private static List<Resource> loadFromResourceDetectors(SpiHelper spiHelper) {
    List<ComponentProvider> componentProviders = spiHelper.load(ComponentProvider.class);
    List<ResourceAndOrder> resourceAndOrders = new ArrayList<>();
    for (ComponentProvider<?> componentProvider : componentProviders) {
      if (componentProvider.getType() != Resource.class) {
        continue;
      }
      Resource resource;
      try {
        resource = (Resource) componentProvider.create(EMPTY_CONFIG);
      } catch (Throwable throwable) {
        throw new ConfigurationException(
            "Error configuring "
                + Resource.class.getName()
                + " with name \""
                + componentProvider.getName()
                + "\"",
            throwable);
      }
      int order =
          (componentProvider instanceof Ordered) ? ((Ordered) componentProvider).order() : 0;
      resourceAndOrders.add(new ResourceAndOrder(resource, order));
    }
    resourceAndOrders.sort(Comparator.comparing(ResourceAndOrder::order));
    return resourceAndOrders.stream().map(ResourceAndOrder::resource).collect(Collectors.toList());
  }

  private static final class ResourceAndOrder {
    private final Resource resource;
    private final int order;

    private ResourceAndOrder(Resource resource, int order) {
      this.resource = resource;
      this.order = order;
    }

    private Resource resource() {
      return resource;
    }

    private int order() {
      return order;
    }
  }
}
