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
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;
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
        Arguments.argumentSet("plus sign preserved", "food=cheese+cake", "food", "cheese+cake"),
        Arguments.argumentSet("percent-encoded space", "key=hello%20world", "key", "hello world"),
        Arguments.argumentSet("invalid percent encoding", "key=abc%2Gdef", "key", "abc%2Gdef"),
        Arguments.argumentSet("incomplete percent encoding", "key=abc%2", "key", "abc%2"),
        Arguments.argumentSet("percent at end", "key=abc%", "key", "abc%"),
        Arguments.argumentSet("multiple percent encodings", "key=a%20b%2Bc%3Dd", "key", "a b+c=d"),
        Arguments.argumentSet("no percent encoding", "key=plain-value", "key", "plain-value"));
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
                        "otel.resource.attributes", "service.name=myService,appName=MyApp")))
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
                    singletonMap("otel.service.name", "myService")))
            .getAttributes();

    assertThat(attributes).hasSize(1).containsEntry(stringKey("service.name"), "myService");
  }

  @Test
  void createEnvironmentResource_ServiceNamePriority() {
    Attributes attributes =
        ResourceConfiguration.createEnvironmentResource(
                DefaultConfigProperties.createFromMap(
                    ImmutableMap.of(
                        "otel.resource.attributes",
                        "service.name=myService,appName=MyApp",
                        "otel.service.name",
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

  @Test
  void createEnvironmentResource_EntitiesEnabled() {
    Map<String, String> props = new HashMap<>();
    props.put(EntityExperimentConstants.EXPERIMENTAL_ENTITIES_ENABLED, "true");
    props.put(
            EnvironmentResource.ENTITIES_PROPERTY,
                "process{process.pid=1234}[process.executable.name=java]@http://schema;host{host.id=myhost}");
    props.put("otel.service.name", "my-service");
    props.put("otel.resource.attributes", "flat.attr=flat-val");

    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(props));

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).hasSize(3);

    assertThat(entities)
        .anyMatch(
            e ->
                e.getType().equals("process")
                    && e.getSchemaUrl().equals("http://schema")
                    && e.getId().equals(Attributes.of(stringKey("process.pid"), "1234"))
                    && e.getDescription()
                        .equals(Attributes.of(stringKey("process.executable.name"), "java")));

    assertThat(entities)
        .anyMatch(
            e ->
                e.getType().equals("host")
                    && e.getSchemaUrl() == null
                    && e.getId().equals(Attributes.of(stringKey("host.id"), "myhost")));

    assertThat(entities)
        .anyMatch(
            e ->
                e.getType().equals("service")
                    && e.getId().equals(Attributes.of(stringKey("service.name"), "my-service")));

    // The attributes of the entities should also be flat attributes on the Resource
    assertThat(resource.getAttributes())
        .containsEntry(stringKey("service.name"), "my-service")
        .containsEntry("process.pid", "1234")
        .containsEntry("process.executable.name", "java")
        .containsEntry("host.id", "myhost")
        .containsEntry("flat.attr", "flat-val");
  }

  @Test
  void createEnvironmentResource_EntitiesDisabled() {
    Map<String, String> props = new HashMap<>();
    props.put(EntityExperimentConstants.EXPERIMENTAL_ENTITIES_ENABLED, "false");
    props.put(EnvironmentResource.ENTITIES_PROPERTY, "process{process.pid=1234}");
    props.put("otel.service.name", "my-service");
    props.put("otel.resource.attributes", "flat.attr=flat-val");

    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(props));

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).isEmpty();

    assertThat(resource.getAttributes())
        .containsEntry(stringKey("service.name"), "my-service")
        .containsEntry("flat.attr", "flat-val")
        // otel.entities is ignored when entities are disabled
        .doesNotContainKey(stringKey("process.pid"));
  }
}
