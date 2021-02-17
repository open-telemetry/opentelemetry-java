/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

final class EnvironmentResource {

  private static final Resource INSTANCE = buildResource();

  // Visible for testing
  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";

  static Resource getInstance() {
    return INSTANCE;
  }

  private static Resource buildResource() {
    return Resource.create(getAttributes(ConfigProperties.get()));
  }

  // visible for testing
  static Attributes getAttributes(ConfigProperties configProperties) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    configProperties.getCommaSeparatedMap(ATTRIBUTE_PROPERTY).forEach(resourceAttributes::put);
    return resourceAttributes.build();
  }

  private EnvironmentResource() {}
}
