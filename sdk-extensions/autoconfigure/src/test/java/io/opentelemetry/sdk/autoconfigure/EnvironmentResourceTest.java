/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.junit.jupiter.api.Test;

class EnvironmentResourceTest {

  @Test
  void resourceFromConfig_empty() {
    Attributes attributes =
        new EnvironmentResource().getAttributes(ConfigProperties.createForTest(emptyMap()));

    assertThat(attributes).isEqualTo(Attributes.empty());
  }

  @Test
  void resourceFromConfig() {
    Attributes attributes =
        new EnvironmentResource()
            .getAttributes(
                ConfigProperties.createForTest(
                    singletonMap(
                        EnvironmentResource.ATTRIBUTE_PROPERTY,
                        "service.name=myService,appName=MyApp")));

    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.SERVICE_NAME,
                "myService",
                AttributeKey.stringKey("appName"),
                "MyApp"));
  }

  @Test
  void resourceFromConfig_emptyEnvVar() {
    Attributes attributes =
        new EnvironmentResource()
            .getAttributes(
                ConfigProperties.createForTest(
                    singletonMap(EnvironmentResource.ATTRIBUTE_PROPERTY, "")));

    assertThat(attributes).isEqualTo(Attributes.empty());
  }
}
