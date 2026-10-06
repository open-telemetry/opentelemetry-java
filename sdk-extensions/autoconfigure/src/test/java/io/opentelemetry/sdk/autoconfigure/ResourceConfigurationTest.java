/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.autoconfigure.ResourceConfiguration.DISABLED_ATTRIBUTE_KEYS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.event.Level.INFO;

import com.google.common.collect.ImmutableMap;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceConfigurationTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForType(ResourceBuilder.class).captureForType(EntityUtil.class);

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
  @MethodSource("entityResourceProviderOrderTestCases")
  void entityResourceProviderOrder(int firstOrder, int secondOrder, String expectedId) {
    ResourceProvider first = resourceProvider("H1", firstOrder);
    ResourceProvider second = resourceProvider("H2", secondOrder);
    ComponentLoader loader = mock(ComponentLoader.class);
    when(loader.load(ResourceProvider.class)).thenReturn(Arrays.asList(first, second));

    Resource resource =
        ResourceConfiguration.configureResource(
            DefaultConfigProperties.createFromMap(Collections.emptyMap()),
            SpiHelper.create(loader),
            (r, c) -> r);

    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder("host", Attributes.of(stringKey("host.id"), expectedId)).build());
    assertThat(resource.getAttributes()).containsEntry("host.id", expectedId);
  }

  static Stream<Arguments> entityResourceProviderOrderTestCases() {
    return Stream.of(
        Arguments.argumentSet("second detector runs last", 1, 2, "H2"),
        Arguments.argumentSet("first detector runs last", 2, 1, "H1"));
  }

  private static ResourceProvider resourceProvider(String hostId, int order) {
    return new ResourceProvider() {
      @Override
      public Resource createResource(ConfigProperties config) {
        return EntityUtil.createResource(
            Collections.singletonList(
                Entity.builder("host", Attributes.of(stringKey("host.id"), hostId)).build()));
      }

      @Override
      public int order() {
        return order;
      }
    };
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
  void disabledKeysFilterEntityAttributes() {
    Entity service =
        Entity.builder("service", Attributes.of(stringKey("service.name"), "original"))
            .setDescription(Attributes.of(stringKey("service.version"), "1.0"))
            .build();
    Resource resource = EntityUtil.createResource(Collections.singletonList(service));
    Resource filtered =
        ResourceConfiguration.filterAttributes(
            resource,
            DefaultConfigProperties.createFromMap(
                singletonMap(DISABLED_ATTRIBUTE_KEYS, "service.name,service.version")));

    assertThat(filtered.getAttributes()).isEmpty();
    assertThat(EntityUtil.getEntities(filtered)).isEmpty();
    assertThat(EntityUtil.getEntities(resource)).containsExactly(service);
    logs.assertContains(
        event -> event.getLevel().equals(INFO), "Removing entity association [service]");
  }

  @ParameterizedTest
  @MethodSource("legacyEntityOverrides")
  void legacyEntityOverride(Map<String, String> overrides, String expectedName) {
    Map<String, String> properties = new HashMap<>(overrides);
    properties.put(
        "otel.entities", "service{service.name=detected}[service.version=1.0];host{host.id=H1}");
    Resource resource =
        ResourceConfiguration.createEnvironmentResource(
            DefaultConfigProperties.createFromMap(properties));

    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build());
    assertThat(EntityUtil.getUnassociatedAttributes(resource))
        .isEqualTo(
            Attributes.builder()
                .put("service.name", expectedName)
                .put("service.version", "1.0")
                .build());
    logs.assertContains(
        event -> event.getLevel().equals(INFO), "Removing entity association [service]");
  }

  static Stream<Arguments> legacyEntityOverrides() {
    return Stream.of(
        Arguments.argumentSet(
            "resource attributes",
            singletonMap("otel.resource.attributes", "service.name=resource"),
            "resource"),
        Arguments.argumentSet(
            "service name", singletonMap("otel.service.name", "configured"), "configured"),
        Arguments.argumentSet(
            "service name beats resource attributes",
            ImmutableMap.of(
                "otel.resource.attributes",
                "service.name=resource",
                "otel.service.name",
                "configured"),
            "configured"));
  }

  @ParameterizedTest
  @MethodSource("createEnvironmentResourceEntitiesTestCases")
  void createEnvironmentResource_WithEntities(
      Map<String, String> properties, Collection<Entity> expectedEntities) {
    ConfigProperties configProperties = DefaultConfigProperties.createFromMap(properties);

    Resource resource = ResourceConfiguration.createEnvironmentResource(configProperties);

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).hasSize(expectedEntities.size());
    assertThat(entities).containsAll(expectedEntities);
  }

  static Stream<Arguments> createEnvironmentResourceEntitiesTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "otel.entities happy path",
            singletonMap(
                "otel.entities",
                "process{process.pid=1234}[process.executable.name=java]@http://schema;host{host.id=myhost}"),
            Arrays.asList(
                Entity.builder("process", Attributes.of(stringKey("process.pid"), "1234"))
                    .setSchemaUrl("http://schema")
                    .setDescription(Attributes.of(stringKey("process.executable.name"), "java"))
                    .build(),
                Entity.builder("host", Attributes.of(stringKey("host.id"), "myhost")).build())),
        Arguments.argumentSet(
            "percent decoding",
            singletonMap(
                "otel.entities",
                "service{service.name=my+app,space=hello%20world,utf8=%C3%A9,invalid=%2G,incomplete=%2,end=%}"),
            Collections.singletonList(
                Entity.builder(
                        "service",
                        Attributes.builder()
                            .put("service.name", "my+app")
                            .put("space", "hello world")
                            .put("utf8", "é")
                            .put("invalid", "%2G")
                            .put("incomplete", "%2")
                            .put("end", "%")
                            .build())
                    .build())),
        Arguments.argumentSet(
            "malformed",
            singletonMap(
                "otel.entities",
                "{empty.type=val};process{};process{=val};process{key;=val};host{host.id=valid}"),
            Collections.singletonList(
                Entity.builder("host", Attributes.builder().put("host.id", "valid").build())
                    .build())),
        Arguments.argumentSet("empty", singletonMap("otel.entities", ""), Collections.emptyList()),
        Arguments.argumentSet("absent", Collections.emptyMap(), Collections.emptyList()));
  }
}
