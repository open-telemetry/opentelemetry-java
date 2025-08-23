/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.entities.EntityProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TestEntityProvider {
  @Test
  void defaults_includeServiceAndSdk() {
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(true).build();
    provider.onChange(resource);

    assertThat(resource.get().getAttributes())
        .containsKey("service.name")
        .containsKey("service.instance.id")
        .containsKey("telemetry.sdk.language")
        .containsKey("telemetry.sdk.name")
        .containsKey("telemetry.sdk.version");
    assertThat(resource.get().getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.34.0");

    assertThat(EntityUtil.getEntities(resource.get()))
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e.getType()).isEqualTo("service"),
            e -> assertThat(e.getType()).isEqualTo("telemetry.sdk"));
  }

  @Test
  void resource_updatesDescription() {
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    provider.onChange(resource);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(resource.get().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("one.desc");
  }

  @Test
  void resource_ignoresNewIds() {
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    provider.onChange(resource);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 2).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(resource.get().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void resource_ignoresNewSchemaUrl() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    provider.onChange(resource);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("one.id", 1).build())
        .withDescription(Attributes.builder().put("one.desc", "desc").build())
        .emit();

    assertThat(resource.get().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void resource_addsNewEntity() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    provider.onChange(resource);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    provider
        .attachOrUpdateEntity("two")
        .setSchemaUrl("two")
        .withId(Attributes.builder().put("two.id", 2).build())
        .emit();

    assertThat(resource.get().getAttributes())
        .hasSize(2)
        .containsKey("one.id")
        .containsKey("two.id");
  }

  @Test
  void resource_removesEntity() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    LatestResourceSupplier resource = new LatestResourceSupplier(200);
    provider.onChange(resource);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();

    assertThat(resource.get().getAttributes()).hasSize(1).containsKey("one.id");

    assertThat(provider.removeEntity("one")).isTrue();
    assertThat(resource.get().getAttributes()).isEmpty();
  }

  @Test
  void entityListener_notifiesOnAdd() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();

    EntityListener listener = mock(EntityListener.class);
    provider.onChange(listener);

    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();
    ArgumentCaptor<EntityState> entityCapture = ArgumentCaptor.forClass(EntityState.class);
    ArgumentCaptor<Resource> resourceCapture = ArgumentCaptor.forClass(Resource.class);
    verify(listener, times(1)).onEntityState(entityCapture.capture(), resourceCapture.capture());
    assertThat(entityCapture.getValue().getType()).isEqualTo("one");
    assertThat(resourceCapture.getValue().getAttributes()).hasSize(1).containsKey("one.id");
  }

  @Test
  void entityListener_notifiesOnRemove() {
    SdkEntityProvider provider = SdkEntityProvider.builder().includeDefaults(false).build();
    provider
        .attachOrUpdateEntity("one")
        .setSchemaUrl("one")
        .withId(Attributes.builder().put("one.id", 1).build())
        .emit();
    EntityListener listener = mock(EntityListener.class);
    provider.onChange(listener);

    provider.removeEntity("one");
    ArgumentCaptor<EntityState> entityCapture = ArgumentCaptor.forClass(EntityState.class);
    ArgumentCaptor<Resource> resourceCapture = ArgumentCaptor.forClass(Resource.class);
    verify(listener, times(1)).onEntityDelete(entityCapture.capture(), resourceCapture.capture());
    assertThat(entityCapture.getValue().getType()).isEqualTo("one");
    assertThat(resourceCapture.getValue().getAttributes()).isEmpty();
  }

  @Test
  void entityListener_initializesAfterTimeout() throws InterruptedException {
    // Because we're using same-thread-executor, we know entity provider blocked
    // until everything started up.
    // Instead we fork the resource detection.
    ExecutorService service = Executors.newSingleThreadExecutor();
    ResourceDetector forever =
        (EntityProvider provider) -> {
          // This will never complete.
          return new CompletableResultCode();
        };
    SdkEntityProvider provider =
        SdkEntityProvider.builder()
            .setListenerExecutorService(service)
            .includeDefaults(false)
            .addDetector(forever)
            .build();
    EntityListener listener = mock(EntityListener.class);
    provider.onChange(listener);
    // Ensure we haven't seen initialization yet (If this is flaky, remove this)
    verify(listener, never()).onResourceInit(any());

    // Wait long enough that initialization has happened.
    Thread.sleep(500);
    ArgumentCaptor<Resource> resourceCapture = ArgumentCaptor.forClass(Resource.class);
    verify(listener, times(1)).onResourceInit(resourceCapture.capture());
    assertThat(resourceCapture.getValue().getAttributes()).isEmpty();
  }
}
