/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.internal.GlobUtil.createGlobPatternPredicate;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
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

    ExperimentalResourceDetectionModel detectionModel = model.getDetectionDevelopment();
    if (detectionModel != null) {
      ResourceBuilder detectedResourceBuilder = Resource.builder();

      List<ExperimentalResourceDetectorModel> detectorModels = detectionModel.getDetectors();
      if (detectorModels != null) {
        for (ExperimentalResourceDetectorModel detectorModel : detectorModels) {
          detectedResourceBuilder.putAll(
              ResourceDetectorFactory.getInstance().create(detectorModel, spiHelper, closeables));
        }
      }

      Predicate<String> detectorAttributeFilter =
          detectorAttributeFilter(detectionModel.getAttributes());
      Attributes filteredDetectedAttributes =
          detectedResourceBuilder.build().getAttributes().toBuilder()
              .removeIf(attributeKey -> !detectorAttributeFilter.test(attributeKey.getKey()))
              .build();

      builder.putAll(filteredDetectedAttributes);
    }

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

  private static boolean matchAll(String attributeKey) {
    return true;
  }

  private static Predicate<String> detectorAttributeFilter(
      @Nullable IncludeExcludeModel includedExcludeModel) {
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
      result = result.or(createGlobPatternPredicate(include));
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
      result = result.and(createGlobPatternPredicate(exclude).negate());
    }
    return result;
  }
}
