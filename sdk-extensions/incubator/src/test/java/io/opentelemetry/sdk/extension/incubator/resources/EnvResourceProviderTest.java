/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvResourceProviderTest {

  @Test
  void getTypeAndName() {
    EnvResourceProvider provider = new EnvResourceProvider();
    assertThat(provider.getType()).isEqualTo(Resource.class);
    assertThat(provider.getName()).isEqualTo("env");
    assertThat(provider.order()).isEqualTo(0);
  }

  @Test
  void createResource_EmptyOrNull() {
    EnvResourceProvider provider = new EnvResourceProvider();

    Resource emptyResource =
        provider.createResource(DefaultConfigProperties.createFromMap(Collections.emptyMap()));
    assertThat(EntityUtil.getEntities(emptyResource)).isEmpty();

    Resource blankResource =
        provider.createResource(
            DefaultConfigProperties.createFromMap(Collections.singletonMap("otel.entities", "")));
    assertThat(EntityUtil.getEntities(blankResource)).isEmpty();
  }

  @Test
  void createResource_WithEntities() {
    Map<String, String> props = new HashMap<>();
    props.put(
        "otel.entities",
        "process{process.pid=1234}[process.executable.name=java]@http://schema;host{host.id=myhost}");

    EnvResourceProvider provider = new EnvResourceProvider();
    Resource resource = provider.createResource(DefaultConfigProperties.createFromMap(props));

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).hasSize(2);

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
  }

  @Test
  void createResource_PercentDecoding() {
    Map<String, String> props = new HashMap<>();
    props.put(
        "otel.entities",
        "service{service.name=my+app,space=hello%20world,utf8=%C3%A9,invalid=%2G,incomplete=%2,end=%}");

    EnvResourceProvider provider = new EnvResourceProvider();
    Resource resource = provider.createResource(DefaultConfigProperties.createFromMap(props));

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).hasSize(1);

    Entity entity = entities.iterator().next();
    assertThat(entity.getId())
        .containsEntry(stringKey("service.name"), "my+app")
        .containsEntry(stringKey("space"), "hello world")
        .containsEntry(stringKey("utf8"), "é")
        .containsEntry(stringKey("invalid"), "%2G")
        .containsEntry(stringKey("incomplete"), "%2")
        .containsEntry(stringKey("end"), "%");
  }

  @Test
  void createResource_Malformed() {
    Map<String, String> props = new HashMap<>();
    props.put(
        "otel.entities",
        "{empty.type=val};process{};process{=val};process{key;=val};host{host.id=valid}");

    EnvResourceProvider provider = new EnvResourceProvider();
    Resource resource = provider.createResource(DefaultConfigProperties.createFromMap(props));

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    // Only the last valid host entity should be parsed
    assertThat(entities).hasSize(1);
    assertThat(entities.iterator().next().getType()).isEqualTo("host");
  }

  @Test
  void create_ComponentProvider() {
    System.setProperty("otel.entities", "service{service.name=my-service}");
    try {
      EnvResourceProvider provider = new EnvResourceProvider();
      Resource resource = provider.create(DeclarativeConfigProperties.empty());
      Collection<Entity> entities = EntityUtil.getEntities(resource);
      assertThat(entities).hasSize(1);
      assertThat(entities)
          .anyMatch(
              e ->
                  e.getType().equals("service")
                      && e.getId().equals(Attributes.of(stringKey("service.name"), "my-service")));
    } finally {
      System.clearProperty("otel.entities");
    }
  }
}
