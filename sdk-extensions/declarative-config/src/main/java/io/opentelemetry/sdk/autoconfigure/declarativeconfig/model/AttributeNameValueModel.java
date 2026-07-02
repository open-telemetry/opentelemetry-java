/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "value", "type"})
@Generated("jsonschema2pojo")
public class AttributeNameValueModel {

  @JsonProperty("name")
  @Nullable
  private String name;

  @JsonProperty("value")
  @Nullable
  private Object value;

  @JsonProperty("type")
  @Nullable
  private AttributeNameValueModel.AttributeType type;

  /**
   * The attribute name.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

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
  @JsonProperty("value")
  @Nullable
  public Object getValue() {
    return value;
  }

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
  @JsonProperty("type")
  @Nullable
  public AttributeNameValueModel.AttributeType getType() {
    return type;
  }

  public AttributeNameValueModel withType(AttributeNameValueModel.AttributeType type) {
    this.type = type;
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
          && (this.type == null ? that.type == null : this.type.equals(that.type));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
  public enum AttributeType {
    STRING("string"),
    BOOL("bool"),
    INT("int"),
    DOUBLE("double"),
    STRING_ARRAY("string_array"),
    BOOL_ARRAY("bool_array"),
    INT_ARRAY("int_array"),
    DOUBLE_ARRAY("double_array");
    private final String value;
    private static final Map<String, AttributeNameValueModel.AttributeType> CONSTANTS =
        new HashMap<String, AttributeNameValueModel.AttributeType>();

    static {
      for (AttributeNameValueModel.AttributeType c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    AttributeType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static AttributeNameValueModel.AttributeType fromValue(String value) {
      AttributeNameValueModel.AttributeType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
