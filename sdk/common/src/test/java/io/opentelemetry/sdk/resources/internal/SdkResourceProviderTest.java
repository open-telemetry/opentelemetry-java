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
    AttributeKey<String> sharedKey = AttributeKey.stringKey("shared");
    Resource first = RESOURCE_A.toBuilder().put(sharedKey, "first").build();
    Resource second = RESOURCE_B.toBuilder().put(sharedKey, "second").build();
    SdkResourceProvider provider =
        SdkResourceProvider.builder()
            .addConstantResource(first)
            .addConstantResource(second)
            .build();

    assertThat(provider.getResource()).isEqualTo(first.merge(second));
    assertThat(provider.getResource().getAttribute(sharedKey)).isEqualTo("second");
  }

  @Test
  void builder_conflictingSchemaUrls_usesSequentialMergeSemantics() {
    Resource first = Resource.create(RESOURCE_A.getAttributes(), "https://example.com/schema/1");
    Resource second = Resource.create(RESOURCE_B.getAttributes(), "https://example.com/schema/2");
    Resource third = Resource.create(Attributes.empty(), "https://example.com/schema/3");
    SdkResourceProviderBuilder builder =
        SdkResourceProvider.builder().addConstantResource(first).addConstantResource(second);

    assertThat(builder.build().getResource().getSchemaUrl()).isNull();

    SdkResourceProvider provider = builder.addConstantResource(third).build();

    assertThat(provider.getResource()).isEqualTo(first.merge(second).merge(third));
    assertThat(provider.getResource().getSchemaUrl()).isEqualTo(third.getSchemaUrl());
  }

  @Test
  void builder_reuse_doesNotChangePreviousProvider() {
    SdkResourceProviderBuilder builder =
        SdkResourceProvider.builder().addConstantResource(RESOURCE_A);
    SdkResourceProvider first = builder.build();
    SdkResourceProvider second = builder.addConstantResource(RESOURCE_B).build();

    assertThat(first.getResource()).isEqualTo(RESOURCE_A);
    assertThat(second.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));
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
  void getResource_allDetectorsResolvedOnceAtCreation() {
    CountingDetector first = new CountingDetector(RESOURCE_A);
    CountingDetector second = new CountingDetector(RESOURCE_B);
    SdkResourceProvider provider =
        SdkResourceProvider.createFromDetectors(Arrays.asList(first, second));

    assertThat(first.invocations.get()).isEqualTo(1);
    assertThat(second.invocations.get()).isEqualTo(1);

    assertThat(provider.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));
    assertThat(provider.getResource()).isEqualTo(RESOURCE_A.merge(RESOURCE_B));

    assertThat(first.invocations.get()).isEqualTo(1);
    assertThat(second.invocations.get()).isEqualTo(1);
  }

  private static final class CountingDetector implements ResourceDetector {
    final AtomicInteger invocations = new AtomicInteger();
    private final Resource resource;

    CountingDetector(Resource resource) {
      this.resource = resource;
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
  }
}
