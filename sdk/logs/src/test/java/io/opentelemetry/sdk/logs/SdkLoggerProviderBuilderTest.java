/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
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

    assertThat(SdkLoggerProviderUtil.getSdkResourceProvider(sdkLoggerProvider).getResource())
        .isEqualTo(Resource.getDefault().merge(customResource));
  }

  @Test
  void resourceProvider_defaultsToAutoWrappedStaticResource() {
    SdkLoggerProvider provider = SdkLoggerProvider.builder().build();

    assertThat(SdkLoggerProviderUtil.getSdkResourceProvider(provider).getResource())
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void setSdkResourceProvider_usedForResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(customResource);

    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder();
    SdkLoggerProviderUtil.setSdkResourceProvider(builder, resourceProvider);
    SdkLoggerProvider provider = builder.build();

    assertThat(SdkLoggerProviderUtil.getSdkResourceProvider(provider)).isSameAs(resourceProvider);
    assertThat(SdkLoggerProviderUtil.getSdkResourceProvider(provider).getResource())
        .isEqualTo(customResource);
  }

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithSetResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder().setResource(customResource);
    SdkLoggerProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
  }

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithAddResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder().addResource(customResource);
    SdkLoggerProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
  }
}
