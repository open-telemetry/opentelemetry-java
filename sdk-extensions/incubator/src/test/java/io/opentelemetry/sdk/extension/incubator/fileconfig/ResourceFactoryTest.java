/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.spy;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.resources.Resource;
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
                    new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                            .Resource()
                        .withAttributes(
                            new Attributes()
                                .withServiceName("my-service")
                                .withAdditionalProperty("key", "val")
                                // Should override shape attribute from ResourceComponentProvider
                                .withAdditionalProperty("shape", "circle")),
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
}
