/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.testing.assertj.AttributesAssert;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressLogger(ResourceConfiguration.class)
class ResourceConfigurationTest {

  private final ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(ResourceConfigurationTest.class.getClassLoader());
  private final SpiHelper spiHelper = SpiHelper.create(componentLoader);

  @Test
  void configureResource_EmptyClassLoader() {
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(Collections.emptyMap(), componentLoader),
                SpiHelper.create(new URLClassLoader(new URL[0], null)),
                (r, c) -> r)
            .getAttributes();

    assertThat(attributes.get(AttributeKey.stringKey("service.name")))
        .isEqualTo("unknown_service:java");
    assertThat(attributes.get(AttributeKey.stringKey("cat"))).isNull();
    assertThat(attributes.get(AttributeKey.stringKey("animal"))).isNull();
    assertThat(attributes.get(AttributeKey.stringKey("color"))).isNull();
  }

  @ParameterizedTest
  @MethodSource("configureResourceArgs")
  void configureResource(
      @Nullable String enabledProviders,
      @Nullable String disabledProviders,
      Consumer<AttributesAssert> attributeAssertion) {
    // build.gradle.kts sets:
    // OTEL_SERVICE_NAME=test
    // OTEL_RESOURCE_ATTRIBUTES=cat=meow
    Map<String, String> config = new HashMap<>();
    if (enabledProviders != null) {
      config.put("otel.java.enabled.resource.providers", enabledProviders);
    }
    if (disabledProviders != null) {
      config.put("otel.java.disabled.resource.providers", disabledProviders);
    }
    Attributes attributes =
        ResourceConfiguration.configureResource(
                DefaultConfigProperties.create(config, componentLoader), spiHelper, (r, c) -> r)
            .getAttributes();

    attributeAssertion.accept(assertThat(attributes));
  }

  private static Stream<Arguments> configureResourceArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "default",
            null,
            null,
            attributeConsumer(
                attr -> attr.containsEntry("service.name", "test").containsEntry("cat", "meow"))),
        Arguments.argumentSet(
            "only enabled animal provider",
            "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider",
            null,
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "unknown_service:java")
                        .doesNotContainKey("cat")
                        .containsEntry("animal", "cat")
                        .doesNotContainKey("color"))),
        Arguments.argumentSet(
            "only disabled color provider",
            null,
            "io.opentelemetry.sdk.autoconfigure.provider.TestColorResourceProvider",
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "test")
                        .containsEntry("cat", "meow")
                        .containsEntry("animal", "cat")
                        .doesNotContainKey("color"))),
        Arguments.argumentSet(
            "enabled animal disabled color",
            "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider",
            "io.opentelemetry.sdk.autoconfigure.provider.TestColorResourceProvider",
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "unknown_service:java")
                        .doesNotContainKey("cat")
                        .containsEntry("animal", "cat")
                        .doesNotContainKey("color"))),
        Arguments.argumentSet(
            "enabled animal disabled color and animal",
            "io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider",
            "io.opentelemetry.sdk.autoconfigure.provider.TestColorResourceProvider,io.opentelemetry.sdk.autoconfigure.provider.TestAnimalResourceProvider",
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "unknown_service:java")
                        .doesNotContainKey("cat")
                        .doesNotContainKey("animal")
                        .doesNotContainKey("color"))),
        Arguments.argumentSet(
            "only environment provider enabled",
            "io.opentelemetry.sdk.autoconfigure.EnvironmentResourceProvider",
            null,
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "test")
                        .containsEntry("cat", "meow")
                        .doesNotContainKey("animal")
                        .doesNotContainKey("color"))),
        Arguments.argumentSet(
            "environment provider disabled",
            null,
            "io.opentelemetry.sdk.autoconfigure.EnvironmentResourceProvider",
            attributeConsumer(
                attr ->
                    attr.containsEntry("service.name", "unknown_service:java")
                        .doesNotContainKey("cat")
                        .containsEntry("animal", "cat")
                        .containsEntry("color", "blue"))));
  }

  private static Consumer<AttributesAssert> attributeConsumer(
      Consumer<AttributesAssert> attributesAssertConsumer) {
    return attributesAssertConsumer;
  }
}
