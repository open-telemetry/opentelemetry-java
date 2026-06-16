/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "value", "type"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class AttributeNameValueModel {

  /**
   * The attribute name. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription("The attribute name.\nProperty is required and must be non-null.\n")
  @Nonnull
  private String name;

  /**
   * The attribute value. The type of value must match .type. Property must be present, but if null
   * the entry is ignored.
   *
   * <p>(Required)
   */
  @JsonProperty("value")
  @JsonPropertyDescription(
      "The attribute value.\nThe type of value must match .type.\nProperty must be present, but if null the entry is ignored.\n")
  @Nonnull
  private Object value;

  /** (Can be null) */
  @Nullable
  @JsonProperty("type")
  private AttributeNameValueModel.AttributeType type;

  /**
   * The attribute name. Property is required and must be non-null.
   *
   * <p>(Required)
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
   * The attribute value. The type of value must match .type. Property must be present, but if null
   * the entry is ignored.
   *
   * <p>(Required)
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
    StringBuilder sb = new StringBuilder();
    sb.append(AttributeNameValueModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("name");
    sb.append('=');
    sb.append(((this.name == null) ? "<null>" : this.name));
    sb.append(',');
    sb.append("value");
    sb.append('=');
    sb.append(((this.value == null) ? "<null>" : this.value));
    sb.append(',');
    sb.append("type");
    sb.append('=');
    sb.append(((this.type == null) ? "<null>" : this.type));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
    result = ((result * 31) + ((this.type == null) ? 0 : this.type.hashCode()));
    result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof AttributeNameValueModel) == false) {
      return false;
    }
    AttributeNameValueModel rhs = ((AttributeNameValueModel) other);
    return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
            && ((this.type == rhs.type) || ((this.type != null) && this.type.equals(rhs.type))))
        && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
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
