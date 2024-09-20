/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.spy;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ResourceFactoryTest {

  private SpiHelper spiHelper = SpiHelper.create(MetricExporterFactoryTest.class.getClassLoader());

  @Test
  void create() {
    spiHelper = spy(spiHelper);
    assertThat(
            ResourceFactory.getInstance()
                .create(
                    new ResourceModel()
                        .withAttributes(
                            Arrays.asList(
                                new AttributeNameValueModel()
                                    .withName("service.name")
                                    .withValue("my-service"),
                                new AttributeNameValueModel().withName("key").withValue("val"),
                                new AttributeNameValueModel()
                                    .withName("shape")
                                    .withValue("circle"))),
                    spiHelper,
                    Collections.emptyList()))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put("service.name", "my-service")
                .put("key", "val")
                .put("shape", "circle")
                // From ResourceComponentProvider
                .put("color", "red")
                // From ResourceOrderedSecondComponentProvider, which takes priority over
                // ResourceOrderedFirstComponentProvider
                .put("order", "second")
                .build());
  }

  // TODO: add test coverage for .attributes_list, .detectors
}
