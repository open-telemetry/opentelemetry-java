/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.NameStringValuePairModel.NAME;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.NameStringValuePairModel.VALUE;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({NAME, VALUE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class NameStringValuePairModel {

  static final String NAME = "name";
  static final String VALUE = "value";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(NAME, String.class);
    STABLE_PROPERTIES.put(VALUE, String.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String name;
  @Nullable private String value;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * The name of the pair.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(NAME)
  @Nullable
  public String getName() {
    if (name == null) {
      return ExtensionPropertyUtil.getGraduated(NAME, extensionProperties, String.class);
    }
    return name;
  }

  @JsonProperty(NAME)
  public NameStringValuePairModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The value of the pair.
   *
   * <p>Property must be present, but if null the behavior is dependent on usage context.
   */
  @JsonProperty(VALUE)
  @Nullable
  public String getValue() {
    if (value == null) {
      return ExtensionPropertyUtil.getGraduated(VALUE, extensionProperties, String.class);
    }
    return value;
  }

  @JsonProperty(VALUE)
  public NameStringValuePairModel withValue(String value) {
    this.value = value;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public NameStringValuePairModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "NameStringValuePairModel{"
        + "name="
        + name
        + ", value="
        + value
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.name == null) ? 0 : this.name.hashCode();
    h *= 1000003;
    h ^= (this.value == null) ? 0 : this.value.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof NameStringValuePairModel) {
      NameStringValuePairModel that = (NameStringValuePairModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.value == null ? that.value == null : this.value.equals(that.value))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
