/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceInstanceIdResourceProviderTest {

  private static final AttributeKey<String> SERVICE_INSTANCE_ID = stringKey("service.instance.id");

  @Test
  void createResource() {
    DefaultConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());

    Resource resource = new ServiceInstanceIdResourceProvider().createResource(config);
    String serviceInstanceId = resource.getAttribute(SERVICE_INSTANCE_ID);
    assertThat(serviceInstanceId).isNotNull();
    assertThat(UUID.fromString(serviceInstanceId)).isNotNull();
    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder(
                    "service.instance", Attributes.of(SERVICE_INSTANCE_ID, serviceInstanceId))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build());
    assertThat(EntityUtil.getUnassociatedAttributes(resource)).isEmpty();
    assertThat(resource.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.40.0");

    // Stable across calls and instances
    assertThat(
            new ServiceInstanceIdResourceProvider()
                .createResource(config)
                .getAttribute(SERVICE_INSTANCE_ID))
        .isEqualTo(serviceInstanceId);
    assertThat(new ServiceInstanceIdResourceProvider().createResource(config)).isEqualTo(resource);
  }

  @Test
  void order_returnsMinValue() {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    assertThat(provider.order()).isEqualTo(Integer.MIN_VALUE);
  }
}
