/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EntitiesEnvResourceDetectorTest {

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(SpanProcessorFactoryTest.class.getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArgs")
  void create(Map<String, String> systemProperties, Collection<Entity> expectedEntities) {
    systemProperties.forEach(System::setProperty);
    try {
      ResourceModel resourceModel =
          new ResourceModel()
              .withDetectionDevelopment(
                  new ExperimentalResourceDetectionModel()
                      .withDetectors(
                          Collections.singletonList(
                              new ExperimentalResourceDetectorModel()
                                  .withAdditionalProperty("env", null))));
      Resource resource = ResourceFactory.getInstance().create(resourceModel, context);
      Collection<Entity> entities = EntityUtil.getEntities(resource);
      assertThat(entities).hasSize(expectedEntities.size());
      assertThat(entities).containsAll(expectedEntities);
    } finally {
      systemProperties.forEach((key, unused) -> System.clearProperty(key));
    }
  }

  static Stream<Arguments> createArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "happy path",
            ImmutableMap.of(
                "otel.entities",
                "process{process.pid=1234}[process.executable.name=java]@http://schema;host{host.id=myhost}"),
            Arrays.asList(
                Entity.builder("process", Attributes.of(stringKey("process.pid"), "1234"))
                    .setSchemaUrl("http://schema")
                    .setDescription(Attributes.of(stringKey("process.executable.name"), "java"))
                    .build(),
                Entity.builder("host", Attributes.of(stringKey("host.id"), "myhost")).build())),
        Arguments.argumentSet(
            "empty", ImmutableMap.of("otel.entities", ""), Collections.emptyList()));
  }
}
