/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
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
  public Resource create(ResourceModel model, DeclarativeConfigContext context) {
    ResourceBuilder builder = Resource.getDefault().toBuilder();

    ExperimentalResourceDetectionModel detectionModel = model.getDetectionDevelopment();
    if (detectionModel != null) {
      ResourceBuilder detectedResourceBuilder = Resource.builder();

      List<ExperimentalResourceDetectorModel> detectorModels = detectionModel.getDetectors();
      if (detectorModels != null) {
        for (ExperimentalResourceDetectorModel detectorModel : detectorModels) {
          detectedResourceBuilder.putAll(
              ResourceDetectorFactory.getInstance().create(detectorModel, context));
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
          .putAll(AttributeListFactory.getInstance().create(attributeNameValueModel, context))
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
    return IncludeExcludePredicate.createPatternMatching(included, excluded);
  }
}
