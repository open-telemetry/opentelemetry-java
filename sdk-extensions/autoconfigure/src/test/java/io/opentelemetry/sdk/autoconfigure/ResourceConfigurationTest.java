/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.autoconfigure.ResourceConfiguration.DISABLED_ATTRIBUTE_KEYS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonMap;
import static org.slf4j.event.Level.WARN;

import com.google.common.collect.ImmutableMap;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceConfigurationTest {

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(ResourceBuilder.class);

  private static final ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(ResourceConfigurationTest.class.getClassLoader());

  @Test
  void customConfigResourceWithDisabledKeys() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put(
        "otel.resource.attributes", "food=cheesecake,drink=juice,animal=  ,color=,shape=square");
    props.put("otel.resource.disabled-keys", "drink");
    ConfigProperties config = DefaultConfigProperties.create(props, componentLoader);

    assertThat(
            ResourceConfiguration.configureResource(
                config,
                SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
                (r, c) -> r))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .putAll(new ServiceInstanceIdResourceProvider().createResource(config))
                .putAll(
                    EntityUtil.createResource(
                        Collections.singletonList(serviceEntity("test-service"))))
                .put("food", "cheesecake")
                .put("shape", "square")
                .build());
  }

  @Test
  void serviceInstanceIdFallback_defaultBehavior() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");

    Resource result =
        ResourceConfiguration.configureResource(
            DefaultConfigProperties.create(props, componentLoader),
            SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
            (r, c) -> r);

    String serviceInstanceId = result.getAttribute(stringKey("service.instance.id"));
    assertThat(serviceInstanceId).isNotNull();
    assertThat(UUID.fromString(serviceInstanceId)).isNotNull();
    assertThat(result.getAttribute(stringKey("service.name"))).isEqualTo("test-service");
    assertThat(EntityUtil.getEntities(result))
        .containsExactlyInAnyOrder(
            serviceEntity("test-service"),
            Entity.builder(
                    "service.instance",
                    Attributes.of(stringKey("service.instance.id"), serviceInstanceId))
                .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
                .build());
  }

  @Test
  @SuppressLogger(ResourceBuilder.class)
  void serviceInstanceIdExplicitValuePreserved() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put("otel.resource.attributes", "service.instance.id=my-custom-id-123");

    Resource result =
        ResourceConfiguration.configureResource(
            DefaultConfigProperties.create(props, componentLoader),
            SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
            (r, c) -> r);

    assertThat(result.getAttribute(stringKey("service.instance.id"))).isEqualTo("my-custom-id-123");
    assertThat(EntityUtil.getEntities(result)).containsExactly(serviceEntity("test-service"));
    assertThat(EntityUtil.getUnassociatedAttributes(result))
        .containsEntry(stringKey("service.instance.id"), "my-custom-id-123");
  }

  @Test
  @SuppressLogger(ResourceBuilder.class)
  void serviceInstanceEntityOverridesFallback() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            singletonMap("otel.entities", "service.instance{service.instance.id=custom}"));
    Resource result =
        ResourceConfiguration.configureResource(
            config,
            SpiHelper.create(ResourceConfigurationTest.class.getClassLoader()),
            (r, c) -> r);

    assertThat(EntityUtil.getEntities(result))
        .containsExactly(
            Entity.builder(
                    "service.instance", Attributes.of(stringKey("service.instance.id"), "custom"))
                .build());
    assertThat(result.getAttribute(stringKey("service.instance.id"))).isEqualTo("custom");
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
        Arguments.argumentSet("plus sign preserved", "food=cheese+cake", "food", "cheese+cake"),
        Arguments.argumentSet("percent-encoded space", "key=hello%20world", "key", "hello world"),
        Arguments.argumentSet("invalid percent encoding", "key=abc%2Gdef", "key", "abc%2Gdef"),
        Arguments.argumentSet("incomplete percent encoding", "key=abc%2", "key", "abc%2"),
        Arguments.argumentSet("percent at end", "key=abc%", "key", "abc%"),
        Arguments.argumentSet("multiple percent encodings", "key=a%20b%2Bc%3Dd", "key", "a b+c=d"),
        Arguments.argumentSet("no percent encoding", "key=plain-value", "key", "plain-value"),
        Arguments.argumentSet(
            "unencoded non-ASCII with percent encoding", "key=café%20bar", "key", "café bar"),
        Arguments.argumentSet(
            "encoded and unencoded multi-byte", "key=%C3%A9t%C3%A9 été", "key", "été été"),
        Arguments.argumentSet(
            "unencoded supplementary character with percent encoding",
            "key=😀%20x",
            "key",
            "😀 x"));
  }

  @Test
  void createEnvironmentResource_Empty() {
    Attributes attributes = ResourceConfiguration.createEnvironmentResource().getAttributes();

    assertThat(attributes).isEmpty();
  }

  @Test
  void createEnvironmentResource_WithResourceAttributes() {
    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(
                singletonMap(
                    "otel.resource.attributes",
                    "service.name=myService,service.instance.id=custom,appName=MyApp")));
    Attributes attributes = resource.getAttributes();
    assertThat(EntityUtil.getEntities(resource)).isEmpty();
    assertThat(EntityUtil.getUnassociatedAttributes(resource)).isEqualTo(attributes);

    assertThat(attributes)
        .hasSize(3)
        .containsEntry(stringKey("service.name"), "myService")
        .containsEntry(stringKey("service.instance.id"), "custom")
        .containsEntry("appName", "MyApp");
  }

  @Test
  @SetSystemProperty(key = "otel.service.name", value = "myService")
  void createEnvironmentResource_WithServiceName() {
    Resource resource = ResourceConfiguration.createEnvironmentResource();

    assertThat(resource.getAttributes())
        .hasSize(1)
        .containsEntry(stringKey("service.name"), "myService");
    assertThat(EntityUtil.getEntities(resource)).containsExactly(serviceEntity("myService"));
    assertThat(EntityUtil.getUnassociatedAttributes(resource)).isEmpty();
    assertThat(resource.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.40.0");
  }

  @Test
  void createEnvironmentResource_ServiceNamePriority() {
    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(
                ImmutableMap.of(
                    "otel.resource.attributes",
                    "service.name=myService,appName=MyApp",
                    "otel.service.name",
                    "ReallyMyService")));
    Attributes attributes = resource.getAttributes();
    assertThat(EntityUtil.getEntities(resource)).containsExactly(serviceEntity("ReallyMyService"));

    assertThat(attributes)
        .hasSize(2)
        .containsEntry(stringKey("service.name"), "ReallyMyService")
        .containsEntry("appName", "MyApp");
  }

  @Test
  void createEnvironmentResource_EmptyResourceAttributes() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(singletonMap("otel.resource.attributes", "")))
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

  @ParameterizedTest
  @MethodSource("legacyEntityOverrides")
  @SuppressLogger(ResourceBuilder.class)
  void legacyEntityOverride(Map<String, String> overrides, Resource expectedResource) {
    Map<String, String> properties = new HashMap<>(overrides);
    properties.put(
        "otel.entities", "service{service.name=detected}[service.version=1.0];host{host.id=H1}");
    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(properties));

    assertThat(resource).isEqualTo(expectedResource);
    logs.assertContains(
        event -> event.getLevel().equals(WARN),
        overrides.containsKey("otel.resource.attributes")
            ? "Removing entity association [service]"
            : "Replacing entity [service]");
  }

  static Stream<Arguments> legacyEntityOverrides() {
    Entity host = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build();
    Resource configuredService =
        EntityUtil.createResource(Arrays.asList(host, serviceEntity("configured")));
    return Stream.of(
        Arguments.argumentSet(
            "resource attributes",
            singletonMap("otel.resource.attributes", "service.name=resource"),
            EntityUtil.createResource(Collections.singletonList(host)).toBuilder()
                .put("service.name", "resource")
                .put("service.version", "1.0")
                .build()),
        Arguments.argumentSet(
            "service name", singletonMap("otel.service.name", "configured"), configuredService),
        Arguments.argumentSet(
            "service name beats resource attributes",
            ImmutableMap.of(
                "otel.resource.attributes",
                "service.name=resource",
                "otel.service.name",
                "configured"),
            configuredService.toBuilder().put("service.version", "1.0").build()));
  }

  private static Entity serviceEntity(String name) {
    return Entity.builder("service", Attributes.of(stringKey("service.name"), name))
        .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
        .build();
  }
}
