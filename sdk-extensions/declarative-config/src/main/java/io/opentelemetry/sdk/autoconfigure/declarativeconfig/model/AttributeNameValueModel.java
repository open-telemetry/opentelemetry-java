/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel.NAME;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel.TYPE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel.VALUE;

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
@JsonPropertyOrder({NAME, VALUE, TYPE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class AttributeNameValueModel {

  static final String NAME = "name";
  static final String VALUE = "value";
  static final String TYPE = "type";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(NAME, String.class);
    STABLE_PROPERTIES.put(VALUE, Object.class);
    STABLE_PROPERTIES.put(TYPE, AttributeTypeModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String name;
  @Nullable private Object value;
  @Nullable private AttributeTypeModel type;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * The attribute name.
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
  public AttributeNameValueModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The attribute value.
   *
   * <p>The type of value must match .type.
   *
   * <p>Property must be present, but if null the entry is ignored.
   */
  @JsonProperty(VALUE)
  @Nullable
  public Object getValue() {
    if (value == null) {
      return ExtensionPropertyUtil.getGraduated(VALUE, extensionProperties, Object.class);
    }
    return value;
  }

  @JsonProperty(VALUE)
  public AttributeNameValueModel withValue(Object value) {
    this.value = value;
    return this;
  }

  /**
   * The attribute type.
   *
   * <p>Values include:
   *
   * <p>* bool: Boolean attribute value.
   *
   * <p>* bool_array: Boolean array attribute value.
   *
   * <p>* double: Double attribute value.
   *
   * <p>* double_array: Double array attribute value.
   *
   * <p>* int: Integer attribute value.
   *
   * <p>* int_array: Integer array attribute value.
   *
   * <p>* string: String attribute value.
   *
   * <p>* string_array: String array attribute value.
   *
   * <p>If omitted, string is used.
   */
  @JsonProperty(TYPE)
  @Nullable
  public AttributeTypeModel getType() {
    if (type == null) {
      return ExtensionPropertyUtil.getGraduated(
          TYPE, extensionProperties, AttributeTypeModel.class);
    }
    return type;
  }

  @JsonProperty(TYPE)
  public AttributeNameValueModel withType(AttributeTypeModel type) {
    this.type = type;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public AttributeNameValueModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "AttributeNameValueModel{"
        + "name="
        + name
        + ", value="
        + value
        + ", type="
        + type
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
    h ^= (this.type == null) ? 0 : this.type.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AttributeNameValueModel) {
      AttributeNameValueModel that = (AttributeNameValueModel) o;
      return (this.name == null ? that.name == null : this.name.equals(that.name))
          && (this.value == null ? that.value == null : this.value.equals(that.value))
          && (this.type == null ? that.type == null : this.type.equals(that.type))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
