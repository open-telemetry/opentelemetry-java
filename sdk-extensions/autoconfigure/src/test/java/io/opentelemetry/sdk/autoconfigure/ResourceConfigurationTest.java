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
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceConfigurationTest {

  private static final ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(ResourceConfigurationTest.class.getClassLoader());

  @Test
  void customConfigResourceWithDisabledKeys() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put(
        "otel.resource.attributes", "food=cheesecake,drink=juice,animal=  ,color=,shape=square");
    props.put("otel.resource.disabled-keys", "drink");

    assertThat(
            ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(props, componentLoader),
                SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
                (r, c) -> r))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put(stringKey("service.name"), "test-service")
                .put("food", "cheesecake")
                .put("shape", "square")
                .build());
  }

  @ParameterizedTest
  @MethodSource("decodeResourceAttributesArgs")
  void decodeResourceAttributes(String input, String expectedKey, String expectedValue) {
    Map<String, String> props = new HashMap<>();
    props.put("otel.resource.attributes", input);

    assertThat(
            ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(props)))
        .isEqualTo(Resource.create(Attributes.of(stringKey(expectedKey), expectedValue)));
  }

  private static Stream<Arguments> decodeResourceAttributesArgs() {
    return Stream.of(
        // Plus sign preserved
        Arguments.of("food=cheese+cake", "food", "cheese+cake"),
        // Percent-encoded space in resource attribute value decoded to space
        Arguments.of("key=hello%20world", "key", "hello world"),
        // Invalid percent encoding preserved
        Arguments.of("key=abc%2Gdef", "key", "abc%2Gdef"),
        // Incomplete percent encoding preserved
        Arguments.of("key=abc%2", "key", "abc%2"),
        // Percent at end preserved
        Arguments.of("key=abc%", "key", "abc%"),
        // Multiple percent encodings
        Arguments.of("key=a%20b%2Bc%3Dd", "key", "a b+c=d"),
        // No percent encoding
        Arguments.of("key=plain-value", "key", "plain-value"));
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
}
