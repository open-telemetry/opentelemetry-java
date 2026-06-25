/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.EnvironmentResource.ATTRIBUTE_PROPERTY;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.EnvironmentResource.createEnvironmentResource;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.resources.EntityDetector;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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
      if (context.isEntitiesEnabled()) {
        List<ExperimentalResourceDetectorModel> detectorModels = detectionModel.getDetectors();
        if (detectorModels != null) {
          List<EntityDetector> detectors = new ArrayList<>();
          for (ExperimentalResourceDetectorModel detectorModel : detectorModels) {
            ConfigKeyValue detectorKeyValue =
                FileConfigUtil.validateSingleKeyValue(context, detectorModel, "resource detector");
            String detectorName = detectorKeyValue.getKey();

            for (EntityDetector detector : context.load(EntityDetector.class)) {
              if (detector.getName().equals(detectorName)
                  || detector.getClass().getName().equals(detectorName)) {
                detectors.add(detector);
              }
            }
          }
          if (!detectors.isEmpty()) {
            Resource detectedEntityResource = detectEntityResource(detectors);
            builder = detectedEntityResource.toBuilder();
          }
        }
      } else {
        ResourceBuilder detectedResourceBuilder = Resource.builder();

        List<ExperimentalResourceDetectorModel> detectorModels = detectionModel.getDetectors();
        if (detectorModels != null) {
          for (ExperimentalResourceDetectorModel detectorModel : detectorModels) {
            detectedResourceBuilder.putAll(
                ResourceDetectorFactory.getInstance().create(detectorModel, context));
          }
        }

        IncludeExcludeModel attributesIncludeExcludeModel = detectionModel.getAttributes();
        Predicate<String> detectorAttributeFilter =
            attributesIncludeExcludeModel == null
                ? ResourceFactory::matchAll
                : IncludeExcludeFactory.getInstance()
                    .create(attributesIncludeExcludeModel, context);

        Attributes filteredDetectedAttributes =
            detectedResourceBuilder.build().getAttributes().toBuilder()
                .removeIf(attributeKey -> !detectorAttributeFilter.test(attributeKey.getKey()))
                .build();

        builder.putAll(filteredDetectedAttributes);
      }
    }

    String attributeList = model.getAttributesList();
    if (attributeList != null) {
      builder.putAll(
          createEnvironmentResource(
              DefaultConfigProperties.createFromMap(
                  Collections.singletonMap(ATTRIBUTE_PROPERTY, attributeList))));
    }

    List<AttributeNameValueModel> attributeNameValueModel = model.getAttributes();
    if (attributeNameValueModel != null) {
      builder
          .putAll(AttributeListFactory.getInstance().create(attributeNameValueModel, context))
          .build();
    }

    if (model.getSchemaUrl() != null) {
      builder.setSchemaUrl(model.getSchemaUrl());
    }

    return builder.build();
  }

  private static Resource detectEntityResource(List<EntityDetector> detectors) {
    ResourceBuilder builder = Resource.builder();
    for (EntityDetector detector : detectors) {
      for (Entity entity :
          detector.detect(DefaultConfigProperties.createFromMap(Collections.emptyMap()))) {
        if (entity != null) {
          EntityUtil.addEntity(builder, entity);
        }
      }
    }
    return builder.build();
  }

  private static boolean matchAll(String attributeKey) {
    return true;
  }
}
