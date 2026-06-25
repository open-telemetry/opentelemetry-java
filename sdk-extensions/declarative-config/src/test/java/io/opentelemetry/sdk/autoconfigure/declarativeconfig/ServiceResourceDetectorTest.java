/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ServiceResourceDetectorTest {

  @Test
  void getTypeAndName() {
    ServiceResourceDetector detector = new ServiceResourceDetector();

    assertThat(detector.getType()).isEqualTo(Resource.class);
    assertThat(detector.getName()).isEqualTo("service");
  }

  @Test
  @ClearSystemProperty(key = "otel.service.name")
  void create_SystemPropertySet() {
    System.setProperty("otel.service.name", "test");

    assertThat(new ServiceResourceDetector().create(DeclarativeConfigProperties.empty()))
        .satisfies(
            resource -> {
              Attributes attributes = resource.getAttributes();
              assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("test");
              assertThatCode(
                      () ->
                          UUID.fromString(
                              Objects.requireNonNull(
                                  attributes.get(AttributeKey.stringKey("service.instance.id")))))
                  .doesNotThrowAnyException();
            });
  }

  @Test
  void create_NoSystemProperty() {
    assertThat(new ServiceResourceDetector().create(DeclarativeConfigProperties.empty()))
        .satisfies(
            resource -> {
              Attributes attributes = resource.getAttributes();
              assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isNull();
              assertThatCode(
                      () ->
                          UUID.fromString(
                              Objects.requireNonNull(
                                  attributes.get(AttributeKey.stringKey("service.instance.id")))))
                  .doesNotThrowAnyException();
            });
  }

  @Test
  @ClearSystemProperty(key = "otel.service.name")
  @ClearSystemProperty(key = "otel.experimental.entities.enabled")
  void create_EntitiesEnabled() {
    System.setProperty("otel.service.name", "my-service");
    System.setProperty("otel.experimental.entities.enabled", "true");

    Resource resource = new ServiceResourceDetector().create(DeclarativeConfigProperties.empty());

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).hasSize(2);

    assertThat(entities)
        .anyMatch(
            e ->
                e.getType().equals("service")
                    && e.getSchemaUrl().equals("https://opentelemetry.io/schemas/1.40.0")
                    && e.getId()
                        .equals(
                            Attributes.of(AttributeKey.stringKey("service.name"), "my-service")));

    assertThat(entities)
        .anyMatch(
            e ->
                e.getType().equals("service.instance")
                    && e.getSchemaUrl().equals("https://opentelemetry.io/schemas/1.40.0")
                    && e.getId().get(AttributeKey.stringKey("service.instance.id")) != null);

    // Flat attributes should also be present
    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("my-service");
    assertThat(attributes.get(AttributeKey.stringKey("service.instance.id"))).isNotNull();
  }

  @Test
  @ClearSystemProperty(key = "otel.service.name")
  @ClearSystemProperty(key = "otel.experimental.entities.enabled")
  void create_EntitiesDisabled() {
    System.setProperty("otel.service.name", "my-service");
    System.setProperty("otel.experimental.entities.enabled", "false");

    Resource resource = new ServiceResourceDetector().create(DeclarativeConfigProperties.empty());

    Collection<Entity> entities = EntityUtil.getEntities(resource);
    assertThat(entities).isEmpty();

    Attributes attributes = resource.getAttributes();
    assertThat(attributes.get(AttributeKey.stringKey("service.name"))).isEqualTo("my-service");
    assertThat(attributes.get(AttributeKey.stringKey("service.instance.id"))).isNotNull();
  }
}
