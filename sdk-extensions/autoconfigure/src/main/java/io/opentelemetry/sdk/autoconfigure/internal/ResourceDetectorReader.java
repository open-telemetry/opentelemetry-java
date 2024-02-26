/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ResourceDetector;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ResourceDetectorReader<D> {

  private final ResourceDetector<D> resourceDetector;

  public class AttributeBuilder implements ResourceDetector.Builder<D> {

    private AttributeBuilder() {}

    @Override
    public <T> AttributeBuilder add(AttributeKey<T> key, Function<D, Optional<T>> getter) {
      attributeGetters.put((AttributeKey) key, Objects.requireNonNull((Function) getter));
      return this;
    }
  }

  private final Map<AttributeKey<Object>, Function<D, Optional<?>>> attributeGetters =
      new HashMap<>();

  public ResourceDetectorReader(ResourceDetector<D> resourceDetector) {
    this.resourceDetector = resourceDetector;
    resourceDetector.registerAttributes(new AttributeBuilder());
  }

  public boolean shouldApply(ConfigProperties config, Resource existing) {
    Map<String, String> resourceAttributes = getResourceAttributes(config);
    return attributeGetters.keySet().stream()
        .allMatch(key -> shouldUpdate(config, existing, key, resourceAttributes));
  }

  public Resource createResource(ConfigProperties config, Resource existing) {
    return resourceDetector
        .readData(config)
        .map(
            data -> {
              Map<String, String> resourceAttributes = getResourceAttributes(config);
              AttributesBuilder builder = Attributes.builder();
              attributeGetters.entrySet().stream()
                  .filter(e -> shouldUpdate(config, existing, e.getKey(), resourceAttributes))
                  .forEach(
                      e ->
                          e.getValue()
                              .apply(data)
                              .ifPresent(value -> putAttribute(builder, e.getKey(), value)));
              return Resource.create(builder.build());
            })
        .orElse(Resource.empty());
  }

  private static <T> void putAttribute(AttributesBuilder builder, AttributeKey<T> key, T value) {
    builder.put(key, value);
  }

  private static Map<String, String> getResourceAttributes(ConfigProperties config) {
    return config.getMap("otel.resource.attributes");
  }

  private static boolean shouldUpdate(
      ConfigProperties config,
      Resource existing,
      AttributeKey<?> key,
      Map<String, String> resourceAttributes) {
    if (resourceAttributes.containsKey(key.getKey())) {
      return false;
    }

    Object value = existing.getAttribute(key);

    if (key.getKey().equals("service.name")) {
      return config.getString("otel.service.name") == null && "unknown_service:java".equals(value);
    }

    return value == null;
  }
}
