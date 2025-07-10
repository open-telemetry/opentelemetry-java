/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.internal.OtelVersion;
import io.opentelemetry.sdk.resources.internal.AttributeCheckUtil;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents a resource, which capture identifying information about the entities
 * for which signals (stats or traces) are reported.
 */
@Immutable
@AutoValue
public abstract class Resource {
  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  private static final AttributeKey<String> TELEMETRY_SDK_LANGUAGE =
      AttributeKey.stringKey("telemetry.sdk.language");
  private static final AttributeKey<String> TELEMETRY_SDK_NAME =
      AttributeKey.stringKey("telemetry.sdk.name");
  private static final AttributeKey<String> TELEMETRY_SDK_VERSION =
      AttributeKey.stringKey("telemetry.sdk.version");

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
                .put(TELEMETRY_SDK_VERSION, OtelVersion.VERSION)
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
   *     ASCII string or exceed {@link AttributeCheckUtil#MAX_LENGTH} characters.
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
   *     ASCII string or exceed {@link AttributeCheckUtil#MAX_LENGTH} characters.
   */
  public static Resource create(Attributes attributes, @Nullable String schemaUrl) {
    return create(attributes, schemaUrl, Collections.emptyList());
  }

  /**
   * Returns a {@link Resource}.
   *
   * @param attributes a map of {@link Attributes} that describe the resource.
   * @param schemaUrl The URL of the OpenTelemetry schema used to create this Resource.
   * @param entities The set of detected {@link Entity}s that participate in this resource.
   * @return a {@code Resource}.
   * @throws NullPointerException if {@code attributes} is null.
   * @throws IllegalArgumentException if attribute key or attribute value is not a valid printable
   *     ASCII string or exceed {@link AttributeCheckUtil#MAX_LENGTH} characters.
   */
  static Resource create(
      Attributes attributes, @Nullable String schemaUrl, Collection<Entity> entities) {
    AttributeCheckUtil.checkAttributes(Objects.requireNonNull(attributes, "attributes"));
    return new AutoValue_Resource(schemaUrl, attributes, entities);
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
   * Returns a map of attributes that describe the resource, not associated with entites.
   *
   * @return a map of attributes.
   */
  abstract Attributes getRawAttributes();

  /**
   * Returns a collectoion of associated entities.
   *
   * @return a collection of entities.
   */
  abstract Collection<Entity> getEntities();

  /**
   * Returns a map of attributes that describe the resource.
   *
   * @return a map of attributes.
   */
  // @Memoized - This breaks nullaway.
  public Attributes getAttributes() {
    AttributesBuilder result = Attributes.builder();
    getEntities()
        .forEach(
            e -> {
              result.putAll(e.getId());
              result.putAll(e.getDescription());
            });
    // In merge rules, raw comes last, so we return these last.
    result.putAll(getRawAttributes());
    return result.build();
  }

  /**
   * Returns the value for a given resource attribute key.
   *
   * @return the value of the attribute with the given key
   */
  @Nullable
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
    return EntityUtil.merge(this, other);
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
    ResourceBuilder resourceBuilder = builder().putAll(this);

    if (this.getSchemaUrl() != null) {
      resourceBuilder.setSchemaUrl(this.getSchemaUrl());
    }

    return resourceBuilder;
  }

  Resource() {}
}
