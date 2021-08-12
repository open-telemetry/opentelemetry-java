/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_LANGUAGE;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_NAME;
import static io.opentelemetry.semconv.resource.attributes.ResourceAttributes.TELEMETRY_SDK_VERSION;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final Logger logger = Logger.getLogger(Resource.class.getName());

  private static final int MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";
  private static final Resource EMPTY = create(Attributes.empty());
  private static final Resource TELEMETRY_SDK;

  /**
   * The MANDATORY Resource instance contains the mandatory attributes that must be used if they are
   * not provided by the Resource that is given to an SDK signal provider.
   */
  private static final Resource MANDATORY =
      create(Attributes.of(SERVICE_NAME, "unknown_service:java"));

  static {
    TELEMETRY_SDK =
        create(
            Attributes.builder()
                .put(TELEMETRY_SDK_NAME, "opentelemetry")
                .put(TELEMETRY_SDK_LANGUAGE, "java")
                .put(TELEMETRY_SDK_VERSION, readVersion())
                .build());
  }

  private static final Resource DEFAULT = MANDATORY.merge(TELEMETRY_SDK);

  /**
   * Returns the default {@link Resource}. This resource contains the default attributes provided by
   * the SDK.
   *
   * @return a {@code Resource}.
   */
  public static Resource getDefault() {
    return DEFAULT;
  }

  /**
   * Returns an empty {@link Resource}. When creating a {@link Resource}, it is strongly recommended
   * to start with {@link Resource#getDefault()} instead of this method to include SDK required
   * attributes.
   *
   * @return an empty {@code Resource}.
   */
  public static Resource empty() {
    return EMPTY;
  }

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
    return create(attributes, null);
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of {@link Attributes} that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link #MAX_LENGTH} characters.
   */
  public static Resource create(Attributes attributes, @Nullable String schemaUrl) {
    checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    return new AutoValue_Resource(schemaUrl, attributes);
  }

  private static String readVersion() {
    Properties properties = new Properties();
    try {
      properties.load(
          Resource.class.getResourceAsStream("/io/opentelemetry/sdk/version.properties"));
    } catch (Exception e) {
      // we left the attribute empty
      return "unknown";
    }
    return properties.getProperty("sdk.version", "unknown");
  }

  /**
   * Returns the URL of the OpenTelemetry schema used by this resource. May be null.
   *
   * @return An OpenTelemetry schema URL.
   * @since 1.4.0
   */
  @Nullable
  public abstract String getSchemaUrl();

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   */
  public abstract Attributes getAttributes();

  /**
   * Returns the value for a given resource attribute key.
   *
   * @return the value of the attribute with the given key
   */
  public <T> T getAttribute(AttributeKey<T> key) {
    return getAttributes().get(key);
  }

  /**
   * Returns a new, merged {@link Resource} by merging the current {@code Resource} with the {@code
   * other} {@code Resource}. In case of a collision, the "other" {@code Resource} takes precedence.
   *
   * @param other the {@code Resource} that will be merged with {@code this}.
   * @return the newly merged {@code Resource}.
   */
  public Resource merge(@Nullable Resource other) {
    if (other == null) {
      return this;
    }

    AttributesBuilder attrBuilder = Attributes.builder();
    attrBuilder.putAll(this.getAttributes());
    attrBuilder.putAll(other.getAttributes());

    if (other.getSchemaUrl() == null) {
      return create(attrBuilder.build(), getSchemaUrl());
    }
    if (getSchemaUrl() == null) {
      return create(attrBuilder.build(), other.getSchemaUrl());
    }
    if (!other.getSchemaUrl().equals(getSchemaUrl())) {
      logger.info(
          "Attempting to merge Resources with different schemaUrls. "
              + "The resulting Resource will have no schemaUrl assigned. Schema 1: "
              + getSchemaUrl()
              + " Schema 2: "
              + other.getSchemaUrl());
      // currently, behavior is undefined if schema URLs don't match. In the future, we may
      // apply schema transformations if possible.
      return create(attrBuilder.build(), null);
    }
    return create(attrBuilder.build(), getSchemaUrl());
  }

  private static void checkAttributes(Attributes attributes) {
    attributes.forEach(
        (key, value) -> {
          Utils.checkArgument(
              isValidAndNotEmpty(key), "Attribute key" + ERROR_MESSAGE_INVALID_CHARS);
          Objects.requireNonNull(value, "Attribute value" + ERROR_MESSAGE_INVALID_VALUE);
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

  /**
   * Returns a new {@link ResourceBuilder} instance for creating arbitrary {@link Resource}.
   *
   * @since 1.1.0
   */
  public static ResourceBuilder builder() {
    return new ResourceBuilder();
  }

  /**
   * Returns a new {@link ResourceBuilder} instance populated with the data of this {@link
   * Resource}.
   *
   * @since 1.1.0
   */
  public ResourceBuilder toBuilder() {
    return builder().putAll(this);
  }

  Resource() {}
}
