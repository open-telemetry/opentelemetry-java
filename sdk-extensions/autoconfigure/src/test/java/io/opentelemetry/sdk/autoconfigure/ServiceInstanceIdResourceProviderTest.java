/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceInstanceIdResourceProviderTest {

  private static final AttributeKey<String> SERVICE_INSTANCE_ID = stringKey("service.instance.id");

  @Test
  void createResource() {
    DefaultConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());

    String serviceInstanceId =
        new ServiceInstanceIdResourceProvider()
            .createResource(config)
            .getAttribute(SERVICE_INSTANCE_ID);
    assertThat(serviceInstanceId).isNotNull();
    assertThat(UUID.fromString(serviceInstanceId)).isNotNull();

    // Stable across calls and instances
    assertThat(
            new ServiceInstanceIdResourceProvider()
                .createResource(config)
                .getAttribute(SERVICE_INSTANCE_ID))
        .isEqualTo(serviceInstanceId);
  }

  @Test
  void order_returnsMinValue() {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    assertThat(provider.order()).isEqualTo(Integer.MIN_VALUE);
  }
}
