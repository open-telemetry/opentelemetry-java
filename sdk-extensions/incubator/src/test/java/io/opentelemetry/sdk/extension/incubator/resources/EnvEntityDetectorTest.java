/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.extension.incubator.resources.internal.SdkEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnvEntityDetectorTest {

  @Test
  void testEmpty() {
    EnvEntityDetector detector = new EnvEntityDetector();
    Collection<Entity> entities =
        detector.detect(DefaultConfigProperties.createFromMap(Collections.emptyMap()));
    assertThat(entities).isEmpty();
  }

  @Test
  void testSingleEntity() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value =
        "service{service.name=my-app,service.instance.id=instance-1}[service.version=1.0.0]";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(1);
    SdkEntity entity = (SdkEntity) entities.iterator().next();
    assertThat(entity.getType()).isEqualTo("service");
    assertThat(entity.getIdentity())
        .isEqualTo(
            Attributes.builder()
                .put("service.name", "my-app")
                .put("service.instance.id", "instance-1")
                .build());
    assertThat(entity.getDescription())
        .isEqualTo(Attributes.builder().put("service.version", "1.0.0").build());
    assertThat(entity.getSchemaUrl()).isNull();
  }

  @Test
  void testMultipleEntitiesWithSchemaUrl() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value =
        "service{service.name=my-app}@https://opentelemetry.io/schemas/1.21.0;host{host.id=host-123}[host.name=web-server-01]";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(2);
    List<Entity> list = new ArrayList<>(entities);

    SdkEntity entity1 = (SdkEntity) list.get(0);
    assertThat(entity1.getType()).isEqualTo("service");
    assertThat(entity1.getIdentity())
        .isEqualTo(Attributes.builder().put("service.name", "my-app").build());
    assertThat(entity1.getSchemaUrl()).isEqualTo("https://opentelemetry.io/schemas/1.21.0");

    SdkEntity entity2 = (SdkEntity) list.get(1);
    assertThat(entity2.getType()).isEqualTo("host");
    assertThat(entity2.getIdentity())
        .isEqualTo(Attributes.builder().put("host.id", "host-123").build());
    assertThat(entity2.getDescription())
        .isEqualTo(Attributes.builder().put("host.name", "web-server-01").build());
    assertThat(entity2.getSchemaUrl()).isNull();
  }

  @Test
  void testPercentDecoding() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value = "service{service.name=my%2Capp}[config=key%3Dvalue%5Bprod%5D]";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(1);
    SdkEntity entity = (SdkEntity) entities.iterator().next();
    assertThat(entity.getType()).isEqualTo("service");
    assertThat(entity.getIdentity())
        .isEqualTo(Attributes.builder().put("service.name", "my,app").build());
    assertThat(entity.getDescription())
        .isEqualTo(Attributes.builder().put("config", "key=value[prod]").build());
  }

  @Test
  void testEmptyStringsIgnored() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value = ";service{service.name=app1};;host{host.id=host-123};";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(2);
  }

  @Test
  void testMalformedSyntax_MissingBrace() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value = "service service.name=app1};host{host.id=host-123}";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    // Should skip the malformed one and process the valid one
    assertThat(entities).hasSize(1);
    SdkEntity entity = (SdkEntity) entities.iterator().next();
    assertThat(entity.getType()).isEqualTo("host");
  }

  @Test
  void testMalformedSyntax_MissingBraceEnd() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value = "service{service.name=app1;host{host.id=host-123}";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(1);
    SdkEntity entity = (SdkEntity) entities.iterator().next();
    assertThat(entity.getType()).isEqualTo("host");
  }

  @Test
  void testMissingRequiredFields_EmptyIdentity() {
    EnvEntityDetector detector = new EnvEntityDetector();
    String value = "service{};host{host.id=host-123}";
    Collection<Entity> entities =
        detector.detect(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.entities", value)));

    assertThat(entities).hasSize(1);
    SdkEntity entity = (SdkEntity) entities.iterator().next();
    assertThat(entity.getType()).isEqualTo("host");
  }
}
