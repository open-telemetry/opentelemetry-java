/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ServiceInstanceIdResourceProviderTest {

  @Test
  void createResource_generatesStableValue() {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    DefaultConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());

    // Multiple calls should return the same value
    Resource resource1 = provider.createResource(config);
    Resource resource2 = provider.createResource(config);

    assertThat(resource1.getAttribute(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID))
        .isNotNull();
    assertThat(resource2.getAttribute(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID))
        .isEqualTo(resource1.getAttribute(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID));
  }

  @Test
  void shouldApply_returnsFalseWhenAlreadySet() {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    DefaultConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());
    Resource existing = Resource.empty();

    assertThat(provider.shouldApply(config, existing)).isTrue();

    Resource withId =
        existing
            .toBuilder()
            .put(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID, "custom")
            .build();

    assertThat(provider.shouldApply(config, withId)).isFalse();
  }

  @Test
  void order_returnsMaxValue() {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    assertThat(provider.order()).isEqualTo(Integer.MAX_VALUE);
  }
}
