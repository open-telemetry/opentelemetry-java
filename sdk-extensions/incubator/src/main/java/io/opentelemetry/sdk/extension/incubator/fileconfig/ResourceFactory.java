/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.internal.GlobUtil.toGlobPatternPredicate;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalDetectorsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class ResourceFactory implements Factory<ResourceModel, Resource> {

  private static final ResourceFactory INSTANCE = new ResourceFactory();

  private ResourceFactory() {}

  static ResourceFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public Resource create(ResourceModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    ResourceBuilder builder = Resource.getDefault().toBuilder();

    ResourceBuilder detectedResourceBuilder = Resource.builder();
    List<Resource> resourceDetectorResources = loadFromResourceDetectors(spiHelper);
    for (Resource resourceProviderResource : resourceDetectorResources) {
      detectedResourceBuilder.putAll(resourceProviderResource);
    }
    Predicate<String> detectorAttributeFilter =
        detectorAttributeFilter(model.getDetectorsDevelopment());
    builder
        .putAll(
            detectedResourceBuilder.build().getAttributes().toBuilder()
                .removeIf(attributeKey -> !detectorAttributeFilter.test(attributeKey.getKey()))
                .build())
        .build();

    String attributeList = model.getAttributesList();
    if (attributeList != null) {
      builder.putAll(
          ResourceConfiguration.createEnvironmentResource(
              DefaultConfigProperties.createFromMap(
                  Collections.singletonMap("otel.resource.attributes", attributeList))));
    }

    List<AttributeNameValueModel> attributeNameValueModel = model.getAttributes();
    if (attributeNameValueModel != null) {
      builder
          .putAll(
              AttributeListFactory.getInstance()
                  .create(attributeNameValueModel, spiHelper, closeables))
          .build();
    }

    return builder.build();
  }

  /**
   * Load resources from resource detectors, in order of lowest priority to highest priority.
   *
   * <p>In declarative configuration, a resource detector is a {@link ComponentProvider} with {@link
   * ComponentProvider#getType()} set to {@link Resource}. Unlike other {@link ComponentProvider}s,
   * the resource detector version does not use {@link ComponentProvider#getName()} (except for
   * debug messages), and {@link ComponentProvider#create(DeclarativeConfigProperties)} is called
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
        resource = (Resource) componentProvider.create(DeclarativeConfigProperties.empty());
      } catch (Throwable throwable) {
        throw new DeclarativeConfigException(
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

  private static boolean matchAll(String attributeKey) {
    return true;
  }

  private static Predicate<String> detectorAttributeFilter(
      @Nullable ExperimentalDetectorsModel detectorsModel) {
    if (detectorsModel == null) {
      return ResourceFactory::matchAll;
    }
    IncludeExcludeModel includedExcludeModel = detectorsModel.getAttributes();
    if (includedExcludeModel == null) {
      return ResourceFactory::matchAll;
    }
    List<String> included = includedExcludeModel.getIncluded();
    List<String> excluded = includedExcludeModel.getExcluded();
    if (included == null && excluded == null) {
      return ResourceFactory::matchAll;
    }
    if (included == null) {
      return excludedPredicate(excluded);
    }
    if (excluded == null) {
      return includedPredicate(included);
    }
    return includedPredicate(included).and(excludedPredicate(excluded));
  }

  /**
   * Returns a predicate which matches strings matching any of the {@code included} glob patterns.
   */
  private static Predicate<String> includedPredicate(List<String> included) {
    Predicate<String> result = attributeKey -> false;
    for (String include : included) {
      result = result.or(toGlobPatternPredicate(include));
    }
    return result;
  }

  /**
   * Returns a predicate which matches strings NOT matching any of the {@code excluded} glob
   * patterns.
   */
  private static Predicate<String> excludedPredicate(List<String> excluded) {
    Predicate<String> result = attributeKey -> true;
    for (String exclude : excluded) {
      result = result.and(toGlobPatternPredicate(exclude).negate());
    }
    return result;
  }
}
