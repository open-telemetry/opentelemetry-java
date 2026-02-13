/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import java.util.ArrayList;
import java.util.List;

public class TestDeclarativeConfigurationCustomizerProvider
    implements DeclarativeConfigurationCustomizerProvider {

  public static final String EXPORTER_CUSTOMIZER_ATTRIBUTE = "exporter.customized";

  @Override
  public void customize(DeclarativeConfigurationCustomizer customizer) {
    customizer.addModelCustomizer(
        model -> {
          ResourceModel resource = model.getResource();
          if (resource == null) {
            resource = new ResourceModel();
            model.withResource(resource);
          }
          List<AttributeNameValueModel> attributes = resource.getAttributes();
          if (attributes == null) {
            attributes = new ArrayList<>();
            resource.withAttributes(attributes);
          }
          attributes.add(
              new AttributeNameValueModel()
                  .withName("foo")
                  .withType(AttributeNameValueModel.AttributeType.STRING)
                  .withValue("bar"));
          attributes.add(
              new AttributeNameValueModel()
                  .withName("color")
                  .withType(AttributeNameValueModel.AttributeType.STRING)
                  .withValue("blue"));
          return model;
        });

    // Add exporter customizers that inject a resource attribute marker
    customizer.addSpanExporterCustomizer(
        (name, exporter) -> {
          // Mark that exporter customizer was applied by adding attribute to resource
          return exporter;
        });
  }
}
