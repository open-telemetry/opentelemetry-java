/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import com.github.f4b6a3.uuid.UuidCreator;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * does not implement {@link ResourceProvider}, because it depends on all attributes discovered by
 * the other providers.
 */
public final class ServiceInstanceIdResourceProvider {

  private static final AttributeKey<String> SERVICE_NAMESPACE =
      AttributeKey.stringKey("service.namespace");

  static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  private static final AttributeKey<String> HOST_ID = AttributeKey.stringKey("host.id");

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  private static final String UNKNOWN_SERVICE = "unknown_service:java";

  private static final AttributeKey<String> TELEMETRY_SDK_NAME =
      AttributeKey.stringKey("telemetry.sdk.name");

  private static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      AttributeKey.stringKey("telemetry.sdk.language");

  private static final AttributeKey<String> K8S_POD_NAME = AttributeKey.stringKey("k8s.pod.name");

  private static final AttributeKey<String> K8S_NAMESPACE_NAME =
      AttributeKey.stringKey("k8s.namespace.name");

  private static final AttributeKey<String> K8S_CONTAINER_NAME =
      AttributeKey.stringKey("k8s.container.name");

  private static final AttributeKey<String> CONTAINER_ID = AttributeKey.stringKey("container.id");

  private static final AttributeKey<String> MACHINE_ID = AttributeKey.stringKey("machine.id");

  private static final UUID SERVICE_INSTANCE_ID_NAMESPACE =
      UUID.fromString("4d63009a-8d0f-11ee-aad7-4c796ed8e320");

  // multiple calls to this resource provider should return the same value
  public static final Resource RANDOM =
      Resource.create(
          Attributes.of(SERVICE_INSTANCE_ID, UuidCreator.getRandomBasedFast().toString()));

  private ServiceInstanceIdResourceProvider() {}

  interface Variant {
    boolean matches(Attributes attributes);

    Resource generate(Attributes attributes);
  }

  private static final List<List<AttributeKey<String>>> GENERATION_VARIANTS =
      Arrays.asList(
          Arrays.asList(
              TELEMETRY_SDK_NAME,
              TELEMETRY_SDK_LANGUAGE,
              K8S_NAMESPACE_NAME,
              K8S_POD_NAME,
              K8S_CONTAINER_NAME),
          Arrays.asList(
              TELEMETRY_SDK_NAME,
              TELEMETRY_SDK_LANGUAGE,
              SERVICE_NAMESPACE,
              SERVICE_NAME,
              CONTAINER_ID),
          Arrays.asList(TELEMETRY_SDK_NAME, TELEMETRY_SDK_LANGUAGE, SERVICE_NAME, CONTAINER_ID),
          Arrays.asList(
              TELEMETRY_SDK_NAME, TELEMETRY_SDK_LANGUAGE, SERVICE_NAMESPACE, SERVICE_NAME, HOST_ID),
          Arrays.asList(TELEMETRY_SDK_NAME, TELEMETRY_SDK_LANGUAGE, SERVICE_NAME, HOST_ID),
          Arrays.asList(
              TELEMETRY_SDK_NAME,
              TELEMETRY_SDK_LANGUAGE,
              SERVICE_NAMESPACE,
              SERVICE_NAME,
              MACHINE_ID),
          Arrays.asList(TELEMETRY_SDK_NAME, TELEMETRY_SDK_LANGUAGE, SERVICE_NAME, MACHINE_ID));

  private static List<Variant> createVariants() {
    List<Variant> result = new ArrayList<>();

    result.add(
        new Variant() {
          @Override
          public boolean matches(Attributes attributes) {
            return "uuid1".equals(attributes.get(SERVICE_INSTANCE_ID));
          }

          @Override
          public Resource generate(Attributes attributes) {
            return RANDOM;
          }

          @Override
          public String toString() {
            return "uuid1";
          }
        });

    result.add(
        new Variant() {
          @Override
          public boolean matches(Attributes attributes) {
            return attributes.get(SERVICE_INSTANCE_ID) != null;
          }

          @Override
          public Resource generate(Attributes attributes) {
            return Resource.empty();
          }

          @Override
          public String toString() {
            return "none";
          }
        });

    for (List<AttributeKey<String>> variant : GENERATION_VARIANTS) {
      result.add(
          new Variant() {
            @Override
            public boolean matches(Attributes attributes) {
              return variant.stream()
                  .allMatch(
                      key -> {
                        Map<AttributeKey<?>, Object> map = attributes.asMap();
                        if (key == SERVICE_NAME) {
                          return !UNKNOWN_SERVICE.equals(map.getOrDefault(key, ""));
                        }
                        return map.containsKey(key);
                      });
            }

            @Override
            public Resource generate(Attributes attributes) {
              String input = variant.stream().map(attributes::get).collect(Collectors.joining("."));
              return Resource.create(
                  Attributes.of(
                      SERVICE_INSTANCE_ID,
                      UuidCreator.getNameBasedSha1(SERVICE_INSTANCE_ID_NAMESPACE, input)
                          .toString()));
            }

            @Override
            public String toString() {
              return variant.stream().map(AttributeKey::getKey).collect(Collectors.joining(","));
            }
          });
    }
    return result;
  }

  private static final List<Variant> VARIANTS = createVariants();

  public static Resource createResource(Attributes attributes) {
    return findVariant(attributes).map(variant -> variant.generate(attributes)).orElse(RANDOM);
  }

  // Visible for testing
  static Optional<Variant> findVariant(Attributes attributes) {
    return VARIANTS.stream().filter(variant -> variant.matches(attributes)).findFirst();
  }
}
