/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.ResourceConfiguration.DISABLED_ATTRIBUTE_KEYS;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceConfigurationTest {

  @Test
  void customConfigResource() {
    Map<String, String> props = new HashMap<>();
    props.put("otel.service.name", "test-service");
    props.put("otel.resource.attributes", "food=cheesecake,drink=juice");
    props.put("otel.experimental.resource.disabled-keys", "drink");

    assertThat(
            ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(props),
                ResourceConfigurationTest.class.getClassLoader(),
                (r, c) -> r))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "test-service")
                .put("food", "cheesecake")
                .setSchemaUrl(ResourceAttributes.SCHEMA_URL)
                .build());
  }

  @Test
  void resourceFromConfig_empty() {
    Attributes attributes =
        ResourceConfiguration.getAttributes(DefaultConfigProperties.createForTest(emptyMap()));

    assertThat(attributes).isEmpty();
  }

  @Test
  void resourceFromConfig() {
    Attributes attributes =
        ResourceConfiguration.getAttributes(
            DefaultConfigProperties.createForTest(
                singletonMap(
                    ResourceConfiguration.ATTRIBUTE_PROPERTY,
                    "service.name=myService,appName=MyApp")));

    assertThat(attributes)
        .hasSize(2)
        .containsEntry(ResourceAttributes.SERVICE_NAME, "myService")
        .containsEntry("appName", "MyApp");
  }

  @Test
  void serviceName() {
    Attributes attributes =
        ResourceConfiguration.getAttributes(
            DefaultConfigProperties.createForTest(
                singletonMap(ResourceConfiguration.SERVICE_NAME_PROPERTY, "myService")));

    assertThat(attributes).hasSize(1).containsEntry(ResourceAttributes.SERVICE_NAME, "myService");
  }

  @Test
  void resourceFromConfig_overrideServiceName() {
    Attributes attributes =
        ResourceConfiguration.getAttributes(
            DefaultConfigProperties.createForTest(
                ImmutableMap.of(
                    ResourceConfiguration.ATTRIBUTE_PROPERTY,
                    "service.name=myService,appName=MyApp",
                    ResourceConfiguration.SERVICE_NAME_PROPERTY,
                    "ReallyMyService")));

    assertThat(attributes)
        .hasSize(2)
        .containsEntry(ResourceAttributes.SERVICE_NAME, "ReallyMyService")
        .containsEntry("appName", "MyApp");
  }

  @Test
  void resourceFromConfig_emptyEnvVar() {
    Attributes attributes =
        ResourceConfiguration.getAttributes(
            DefaultConfigProperties.createForTest(
                singletonMap(ResourceConfiguration.ATTRIBUTE_PROPERTY, "")));

    assertThat(attributes).isEmpty();
  }

  @Test
  void filterAttributes() {
    ConfigProperties configProperties =
        DefaultConfigProperties.createForTest(ImmutableMap.of(DISABLED_ATTRIBUTE_KEYS, "foo,bar"));

    Resource resourceNoSchema =
        Resource.builder().put("foo", "val").put("bar", "val").put("baz", "val").build();
    Resource resourceWithSchema =
        resourceNoSchema.toBuilder().setSchemaUrl("http://example.com").build();

    assertThat(ResourceConfiguration.filterAttributes(resourceNoSchema, configProperties))
        .satisfies(
            resource -> {
              assertThat(resource.getSchemaUrl()).isNull();
              assertThat(resource.getAttributes()).containsEntry("baz", "val");
              assertThat(resource.getAttributes().get(AttributeKey.stringKey("foo"))).isNull();
              assertThat(resource.getAttributes().get(AttributeKey.stringKey("bar"))).isNull();
            });

    assertThat(ResourceConfiguration.filterAttributes(resourceWithSchema, configProperties))
        .satisfies(
            resource -> {
              assertThat(resource.getSchemaUrl()).isEqualTo("http://example.com");
              assertThat(resource.getAttributes()).containsEntry("baz", "val");
              assertThat(resource.getAttributes().get(AttributeKey.stringKey("foo"))).isNull();
              assertThat(resource.getAttributes().get(AttributeKey.stringKey("bar"))).isNull();
            });
  }
}
