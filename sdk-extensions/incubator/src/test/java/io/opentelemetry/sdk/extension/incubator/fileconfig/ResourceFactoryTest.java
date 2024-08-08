/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ResourceFactoryTest {

  @Test
  void create() {
    assertThat(
            ResourceFactory.getInstance()
                .create(
                    new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                            .Resource()
                        .withAttributes(
                            new Attributes()
                                .withServiceName("my-service")
                                .withAdditionalProperty("key", "val")),
                    mock(SpiHelper.class),
                    Collections.emptyList()))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put("service.name", "my-service")
                .put("key", "val")
                .build());
  }
}
