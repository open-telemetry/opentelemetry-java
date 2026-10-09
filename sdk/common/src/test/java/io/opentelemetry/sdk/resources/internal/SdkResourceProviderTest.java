/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SdkResourceProviderTest {

  private static final Resource RESOURCE_A =
      Resource.create(Attributes.of(AttributeKey.stringKey("a"), "1"));
  private static final Resource RESOURCE_B =
      Resource.create(Attributes.of(AttributeKey.stringKey("b"), "2"));

  @Test
  void create_wrapsResourceInConstantDetector() {
    SdkResourceProvider provider = SdkResourceProvider.create(RESOURCE_A);

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A);
  }

  @Test
  void create_nullResource_throws() {
    assertThatThrownBy(() -> SdkResourceProvider.create(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("resource");
  }

  @Test
  void builder_singleConstantResource() {
    SdkResourceProvider provider =
        SdkResourceProvider.builder().addConstantResource(RESOURCE_A).build();

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A);
  }

  @Test
  void builder_multipleConstantResources_mergedInOrder() {
    SdkResourceProvider provider =
        SdkResourceProvider.builder()
            .addConstantResource(RESOURCE_A)
            .addConstantResource(RESOURCE_B)
            .build();

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));
  }

  @Test
  void builder_addConstantResource_named() {
    SdkResourceProvider provider =
        SdkResourceProvider.builder().addConstantResource(RESOURCE_A, "my-detector").build();

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A);
    assertThat(provider.toString()).contains("ConstantResourceDetector{name=my-detector");
  }

  @Test
  void builder_addConstantResource_nullName() {
    SdkResourceProvider provider =
        SdkResourceProvider.builder().addConstantResource(RESOURCE_A, null).build();

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A);
    assertThat(provider.toString()).contains("ConstantResourceDetector{name=null");
  }

  @Test
  void builder_addConstantResource_nullResource_throws() {
    assertThatThrownBy(() -> SdkResourceProvider.builder().addConstantResource(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("resource");
    assertThatThrownBy(() -> SdkResourceProvider.builder().addConstantResource(null, "name"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("resource");
  }

  @Test
  void builder_build_withoutAddConstantResource_throws() {
    assertThatThrownBy(() -> SdkResourceProvider.builder().build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("At least one Resource must be added via addConstantResource");
  }

  @Test
  void getResource_allDetectorsResolvedOnceAtConstruction_regardlessOfShouldReinvoke() {
    // shouldReinvoke has no runtime effect in this iteration; all detectors are resolved once at
    // construction and the merged Resource is cached for the lifetime of the provider.
    CountingDetector constant = new CountingDetector(RESOURCE_A, /* shouldReinvoke= */ false);
    CountingDetector reinvoke = new CountingDetector(RESOURCE_B, /* shouldReinvoke= */ true);
    SdkResourceProvider provider = new SdkResourceProvider(Arrays.asList(constant, reinvoke));

    assertThat(constant.invocations.get()).isEqualTo(1);
    assertThat(reinvoke.invocations.get()).isEqualTo(1);

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));
    assertThat(provider.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));

    assertThat(constant.invocations.get()).isEqualTo(1);
    assertThat(reinvoke.invocations.get()).isEqualTo(1);
  }

  private static final class CountingDetector implements ResourceDetector {
    final AtomicInteger invocations = new AtomicInteger();
    private final Resource resource;
    private final boolean shouldReinvoke;

    CountingDetector(Resource resource, boolean shouldReinvoke) {
      this.resource = resource;
      this.shouldReinvoke = shouldReinvoke;
    }

    @Override
    public String getName() {
      return "counting";
    }

    @Override
    public Resource getResource() {
      invocations.incrementAndGet();
      return resource;
    }

    @Override
    public boolean shouldReinvoke() {
      return shouldReinvoke;
    }
  }
}
