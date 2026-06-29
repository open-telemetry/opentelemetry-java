/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
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
  void createResource_Empty() {
    EnvResourceProvider provider = new EnvResourceProvider();
    Resource resource =
        provider.createResource(DefaultConfigProperties.createFromMap(Collections.emptyMap()));
    assertThat(EntityUtil.getEntities(resource)).isEmpty();
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
}
