/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.sdk.resources.ResourceAttributes.SDK_LANGUAGE;
import static io.opentelemetry.sdk.resources.ResourceAttributes.SDK_NAME;
import static io.opentelemetry.sdk.resources.ResourceAttributes.SDK_VERSION;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.api.common.AttributeConsumer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final int MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";
  private static final Resource EMPTY = create(Attributes.empty());

  private static final Resource TELEMETRY_SDK;

  static {
    TELEMETRY_SDK =
        create(
            Attributes.builder()
                .put(SDK_NAME, "opentelemetry")
                .put(SDK_LANGUAGE, "java")
                .put(SDK_VERSION, readVersion())
                .build());
  }

  private static final Resource DEFAULT =
      new EnvAutodetectResource.Builder()
          .readEnvironmentVariables()
          .readSystemProperties()
          .build()
          .merge(TELEMETRY_SDK)
          .merge(readResourceFromProviders());

  @Nullable
  private static String readVersion() {
    Properties properties = new Properties();
    try {
      properties.load(
          Resource.class.getResourceAsStream("/io/opentelemetry/sdk/version.properties"));
    } catch (Exception e) {
      // we left the attribute empty
      return "unknown";
    }
    return properties.getProperty("sdk.version");
  }

  private static Resource readResourceFromProviders() {
    ResourcesConfig resourcesConfig =
        ResourcesConfig.builder().readEnvironmentVariables().readSystemProperties().build();
    Resource result = Resource.EMPTY;
    for (ResourceProvider resourceProvider : ServiceLoader.load(ResourceProvider.class)) {
      if (resourcesConfig
          .getDisabledResourceProviders()
          .contains(resourceProvider.getClass().getName())) {
        continue;
      }
      result = result.merge(resourceProvider.create());
    }
    return result;
  }

  Resource() {}

  /**
   * Returns an empty {@link Resource}.
   *
   * @return an empty {@code Resource}.
   */
  public static Resource getEmpty() {
    return EMPTY;
  }

  /**
   * Returns the telemetry sdk {@link Resource}.
   *
   * @return a {@code Resource} with telemetry sdk attributes.
   */
  public static Resource getTelemetrySdk() {
    return TELEMETRY_SDK;
  }

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   */
  public abstract ReadableAttributes getAttributes();

  @Memoized
  @Override
  public abstract int hashCode();

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of attributes that describe the resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   */
  public static Resource create(Attributes attributes) {
    checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    return new AutoValue_Resource(attributes);
  }

  /**
   * Returns a {@link Resource}. This resource information is loaded from the
   * OTEL_RESOURCE_ATTRIBUTES environment variable or otel.resource.attributes system properties.
   *
   * @return a {@code Resource}.
   */
  public static Resource getDefault() {
    return DEFAULT;
  }

  /**
   * Returns a new, merged {@link Resource} by merging the current {@code Resource} with the {@code
   * other} {@code Resource}. In case of a collision, current {@code Resource} takes precedence.
   *
   * @param other the {@code Resource} that will be merged with {@code this}.
   * @return the newly merged {@code Resource}.
   */
  public Resource merge(@Nullable Resource other) {
    if (other == null) {
      return this;
    }

    AttributesBuilder attrBuilder = Attributes.builder();
    Merger merger = new Merger(attrBuilder);
    other.getAttributes().forEach(merger);
    this.getAttributes().forEach(merger);
    return new AutoValue_Resource(attrBuilder.build());
  }

  private static final class Merger implements AttributeConsumer {
    private final AttributesBuilder attrBuilder;

    private Merger(AttributesBuilder attrBuilder) {
      this.attrBuilder = attrBuilder;
    }

    @Override
    public <T> void accept(AttributeKey<T> key, T value) {
      attrBuilder.put(key, value);
    }
  }

  private static void checkAttributes(ReadableAttributes attributes) {
    attributes.forEach(
        new AttributeConsumer() {
          @Override
          public <T> void accept(AttributeKey<T> key, T value) {
            Utils.checkArgument(
                isValidAndNotEmpty(key), "Attribute key" + ERROR_MESSAGE_INVALID_CHARS);
            Objects.requireNonNull(value, "Attribute value" + ERROR_MESSAGE_INVALID_VALUE);
          }
        });
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length not
   * exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValid(String name) {
    return name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length
   * greater than 0 and not exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isValidAndNotEmpty(AttributeKey<?> name) {
    return !name.getKey().isEmpty() && isValid(name.getKey());
  }
}
