/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

/**
 * A builder of {@link Resource} that allows to add key-value pairs and copy attributes from other
 * {@link Attributes} or {@link Resource}.
 *
 * @since 1.1.0
 */
public class ResourceBuilder {

  private final AttributesBuilder attributesBuilder = Attributes.builder();

  /**
   * Puts a String attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, String value) {
    if (key != null && value != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /**
   * Puts a long attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, long value) {
    if (key != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /**
   * Puts a double attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, double value) {
    if (key != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /**
   * Puts a boolean attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, boolean value) {
    if (key != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /**
   * Puts a String array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, String... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
    }
    return this;
  }

  /**
   * Puts a Long array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, long... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
    }
    return this;
  }

  /**
   * Puts a Double array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, double... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
    }
    return this;
  }

  /**
   * Puts a Boolean array attribute into this.
   *
   * <p>Note: It is strongly recommended to use {@link #put(AttributeKey, Object)}, and pre-allocate
   * your keys, if possible.
   *
   * @return this Builder
   */
  public ResourceBuilder put(String key, boolean... values) {
    if (key != null && values != null) {
      attributesBuilder.put(key, values);
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public <T> ResourceBuilder put(AttributeKey<T> key, T value) {
    if (key != null && key.getKey() != null && key.getKey().length() > 0 && value != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /** Puts a {@link AttributeKey} with associated value into this. */
  public ResourceBuilder put(AttributeKey<Long> key, int value) {
    if (key != null && key.getKey() != null) {
      attributesBuilder.put(key, value);
    }
    return this;
  }

  /** Puts all {@link Attributes} into this. */
  public ResourceBuilder putAll(Attributes attributes) {
    if (attributes != null) {
      attributesBuilder.putAll(attributes);
    }
    return this;
  }

  /** Puts all attributes from {@link Resource} into this. */
  public ResourceBuilder putAll(Resource resource) {
    if (resource != null) {
      attributesBuilder.putAll(resource.getAttributes());
    }
    return this;
  }

  /** Create the {@link Resource} from this. */
  public Resource build() {
    return Resource.create(attributesBuilder.build());
  }
}
