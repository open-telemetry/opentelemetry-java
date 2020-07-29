/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
import java.util.Objects;
import java.util.Properties;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 *
 * @since 0.1.0
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
  private static final Resource TELEMETRY_SDK =
      create(
          Attributes.newBuilder()
              .setAttribute("telemetry.sdk.name", "opentelemetry")
              .setAttribute("telemetry.sdk.language", "java")
              .setAttribute("telemetry.sdk.version", readVersion())
              .build());
  private static final Resource DEFAULT =
      new EnvAutodetectResource.Builder().readEnvironmentVariables().readSystemProperties().build();

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

  Resource() {}

  /**
   * Returns an empty {@link Resource}.
   *
   * @return an empty {@code Resource}.
   * @since 0.1.0
   */
  public static Resource getEmpty() {
    return EMPTY;
  }

  /**
   * Returns the telemetry sdk {@link Resource}.
   *
   * @return a {@code Resource} with telemetry sdk attributes.
   * @since 0.6.0
   */
  public static Resource getTelemetrySdk() {
    return TELEMETRY_SDK;
  }

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   * @since 0.1.0
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
   * @since 0.1.0
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
   * @since 0.1.0
   */
  public Resource merge(@Nullable Resource other) {
    if (other == null) {
      return this;
    }

    Attributes.Builder attrBuilder = Attributes.newBuilder();
    Merger merger = new Merger(attrBuilder);
    this.getAttributes().forEach(merger);
    other.getAttributes().forEach(merger);
    return new AutoValue_Resource(attrBuilder.build());
  }

  private static final class Merger implements KeyValueConsumer<AttributeValue> {
    private final Attributes.Builder attrBuilder;

    private Merger(Attributes.Builder attrBuilder) {
      this.attrBuilder = attrBuilder;
    }

    @Override
    public void consume(String key, AttributeValue value) {
      attrBuilder.setAttribute(key, value);
    }
  }

  private static void checkAttributes(ReadableAttributes attributes) {
    attributes.forEach(
        new KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
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
  private static boolean isValidAndNotEmpty(String name) {
    return !name.isEmpty() && isValid(name);
  }
}
