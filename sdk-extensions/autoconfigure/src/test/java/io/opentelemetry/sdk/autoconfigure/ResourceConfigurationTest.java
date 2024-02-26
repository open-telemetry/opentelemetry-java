/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.autoconfigure.ResourceConfiguration.DISABLED_ATTRIBUTE_KEYS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceConfigurationTest {

  @Test
  void customConfigResource() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put(
        "otel.resource.attributes", "food=cheesecake,drink=juice,animal=  ,color=,shape=square");
    props.put("otel.experimental.resource.disabled-keys", "drink");

    assertThat(
            ResourceConfiguration.configureResource(
                DefaultConfigProperties.createFromMap(props),
                SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
                (r, c) -> r))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put(stringKey("service.name"), "test-service")
                .put("food", "cheesecake")
                .put("shape", "square")
                .build());
  }

  @Test
  void createEnvironmentResource_Empty() {
    Attributes attributes = ResourceConfiguration.createEnvironmentResource().getAttributes();

    assertThat(attributes).isEmpty();
  }

  @Test
  void createEnvironmentResource_WithResourceAttributes() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(
                    singletonMap(
                        ResourceConfiguration.ATTRIBUTE_PROPERTY,
                        "service.name=myService,appName=MyApp")))
            .getAttributes();

    assertThat(attributes)
        .hasSize(2)
        .containsEntry(stringKey("service.name"), "myService")
        .containsEntry("appName", "MyApp");
  }

  @Test
  void createEnvironmentResource_WithServiceName() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(
                    singletonMap(ResourceConfiguration.SERVICE_NAME_PROPERTY, "myService")))
            .getAttributes();

    assertThat(attributes).hasSize(1).containsEntry(stringKey("service.name"), "myService");
  }

  @Test
  void createEnvironmentResource_ServiceNamePriority() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(
                    ImmutableMap.of(
                        ResourceConfiguration.ATTRIBUTE_PROPERTY,
                        "service.name=myService,appName=MyApp",
                        ResourceConfiguration.SERVICE_NAME_PROPERTY,
                        "ReallyMyService")))
            .getAttributes();

    assertThat(attributes)
        .hasSize(2)
        .containsEntry(stringKey("service.name"), "ReallyMyService")
        .containsEntry("appName", "MyApp");
  }

  @Test
  void createEnvironmentResource_EmptyResourceAttributes() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(
                    singletonMap(ResourceConfiguration.ATTRIBUTE_PROPERTY, "")))
            .getAttributes();

    assertThat(attributes).isEmpty();
  }

  @Test
  void filterAttributes() {
    ConfigProperties configProperties =
        DefaultConfigProperties.createFromMap(ImmutableMap.of(DISABLED_ATTRIBUTE_KEYS, "foo,bar"));

    Resource resourceNoSchema =
        Resource.builder().put("foo", "val").put("bar", "val").put("baz", "val").build();
    Resource resourceWithSchema =
        resourceNoSchema.toBuilder().setSchemaUrl("http://example.com").build();

    assertThat(ResourceConfiguration.filterAttributes(resourceNoSchema, configProperties))
        .satisfies(
            resource -> {
              assertThat(resource.getSchemaUrl()).isNull();
              assertThat(resource.getAttributes()).containsEntry("baz", "val");
              assertThat(resource.getAttributes().get(stringKey("foo"))).isNull();
              assertThat(resource.getAttributes().get(stringKey("bar"))).isNull();
            });

    assertThat(ResourceConfiguration.filterAttributes(resourceWithSchema, configProperties))
        .satisfies(
            resource -> {
              assertThat(resource.getSchemaUrl()).isEqualTo("http://example.com");
              assertThat(resource.getAttributes()).containsEntry("baz", "val");
              assertThat(resource.getAttributes().get(stringKey("foo"))).isNull();
              assertThat(resource.getAttributes().get(stringKey("bar"))).isNull();
            });
  }

  private static class EnabledTestCase {
    private final String name;
    private final boolean result;
    private final String className;
    private final Set<String> enabledProviders;
    private final Set<String> disabledProviders;
    private final boolean defaultEnabled;
    private final Boolean explicitEnabled;

    private EnabledTestCase(
        String name,
        boolean result,
        String className,
        Set<String> enabledProviders,
        Set<String> disabledProviders,
        boolean defaultEnabled,
        @Nullable Boolean explicitEnabled) {
      this.name = name;
      this.result = result;
      this.className = className;
      this.enabledProviders = enabledProviders;
      this.disabledProviders = disabledProviders;
      this.defaultEnabled = defaultEnabled;
      this.explicitEnabled = explicitEnabled;
    }
  }

  @SuppressWarnings("BooleanParameter")
  @TestFactory
  Stream<DynamicTest> enabledTestCases() {
    return Stream.of(
            new EnabledTestCase(
                "explicitEnabled",
                true,
                "className",
                Collections.emptySet(),
                Collections.emptySet(),
                true,
                true),
            new EnabledTestCase(
                "explicitEnabledFalse",
                false,
                "className",
                Collections.emptySet(),
                Collections.emptySet(),
                true,
                false),
            new EnabledTestCase(
                "enabledProvidersEmpty",
                true,
                "className",
                Collections.emptySet(),
                Collections.emptySet(),
                true,
                null),
            new EnabledTestCase(
                "enabledProvidersContains",
                true,
                "className",
                Collections.singleton("className"),
                Collections.emptySet(),
                true,
                null),
            new EnabledTestCase(
                "enabledProvidersNotContains",
                false,
                "className",
                Collections.singleton("otherClassName"),
                Collections.emptySet(),
                true,
                null),
            new EnabledTestCase(
                "disabledProvidersContains",
                false,
                "className",
                Collections.emptySet(),
                Collections.singleton("className"),
                true,
                null),
            new EnabledTestCase(
                "disabledProvidersNotContains",
                true,
                "className",
                Collections.emptySet(),
                Collections.singleton("otherClassName"),
                true,
                null),
            new EnabledTestCase(
                "defaultEnabledFalse",
                false,
                "className",
                Collections.emptySet(),
                Collections.emptySet(),
                false,
                null))
        .map(
            tc ->
                DynamicTest.dynamicTest(
                    tc.name,
                    () -> {
                      assertThat(
                              ResourceConfiguration.isEnabled(
                                  tc.className,
                                  tc.enabledProviders,
                                  tc.disabledProviders,
                                  tc.defaultEnabled,
                                  tc.explicitEnabled))
                          .isEqualTo(tc.result);
                    }));
  }
}
