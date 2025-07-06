/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

public class SdkLoggerProviderBuilderTest {

  @Test
  void addResource() {
    Resource customResource =
        Resource.create(
            Attributes.of(
                AttributeKey.stringKey("custom_attribute_key"), "custom_attribute_value"));

    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder().addResource(customResource).build();

    // We should find a less invasive way to verify this.
    assertThat(sdkLoggerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault().merge(customResource));
  }

  @Test
  void setResourceSupplier() {
    Resource customResource =
        Resource.create(
            Attributes.of(
                AttributeKey.stringKey("custom_attribute_key"), "custom_attribute_value"));

    SdkLoggerProvider sdkLoggerProvider =
        SdkLoggerProvider.builder().setResourceSupplier(() -> customResource).build();

    // We should find a less invasive way to verify this.
    assertThat(sdkLoggerProvider)
        .extracting("sharedState")
        // Validate the default resource values are NO Longer here when a supplier takes over.
        .hasFieldOrPropertyWithValue("resource", customResource);
  }
}
