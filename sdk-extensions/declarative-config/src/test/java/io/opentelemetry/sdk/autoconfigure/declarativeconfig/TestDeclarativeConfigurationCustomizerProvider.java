/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeTypeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import java.util.ArrayList;
import java.util.List;

public class TestDeclarativeConfigurationCustomizerProvider
    implements DeclarativeConfigurationCustomizerProvider {

  @Override
  public void customize(DeclarativeConfigurationCustomizer customizer) {
    customizer.addModelCustomizer(
        model -> {
          ResourceModel resource = model.getResource();
          if (resource == null) {
            resource = new ResourceModel();
            model.setResource(resource);
          }
          List<AttributeNameValueModel> attributes = resource.getAttributes();
          if (attributes == null) {
            attributes = new ArrayList<>();
            resource.setAttributes(attributes);
          }
          attributes.add(
              new AttributeNameValueModel()
                  .setName("foo")
                  .setType(AttributeTypeModel.STRING)
                  .setValue("bar"));
          attributes.add(
              new AttributeNameValueModel()
                  .setName("color")
                  .setType(AttributeTypeModel.STRING)
                  .setValue("blue"));
          return model;
        });
  }
}
