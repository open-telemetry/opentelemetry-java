/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.ResourceProvider;

public class EnvironmentResource extends ResourceProvider {

  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";

  @Override
  protected Attributes getAttributes() {
    return getAttributes(ConfigProperties.get());
  }

  // visible for testing
  Attributes getAttributes(ConfigProperties configProperties) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    configProperties.getCommaSeparatedMap(ATTRIBUTE_PROPERTY).forEach(resourceAttributes::put);
    return resourceAttributes.build();
  }
}
