/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "value"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class NameStringValuePairModel {

  /**
   * The name of the pair. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription("The name of the pair.\nProperty is required and must be non-null.\n")
  @Nonnull
  private String name;

  /**
   * The value of the pair. Property must be present, but if null the behavior is dependent on usage
   * context.
   *
   * <p>(Required)
   */
  @JsonProperty("value")
  @JsonPropertyDescription(
      "The value of the pair.\nProperty must be present, but if null the behavior is dependent on usage context.\n")
  @Nonnull
  private String value;

  /**
   * The name of the pair. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @Nullable
  public String getName() {
    return name;
  }

  public NameStringValuePairModel withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The value of the pair. Property must be present, but if null the behavior is dependent on usage
   * context.
   *
   * <p>(Required)
   */
  @JsonProperty("value")
  @Nullable
  public String getValue() {
    return value;
  }

  public NameStringValuePairModel withValue(String value) {
    this.value = value;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(NameStringValuePairModel.class.getName())
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
    result = ((result * 31) + ((this.value == null) ? 0 : this.value.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof NameStringValuePairModel) == false) {
      return false;
    }
    NameStringValuePairModel rhs = ((NameStringValuePairModel) other);
    return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
        && ((this.value == rhs.value) || ((this.value != null) && this.value.equals(rhs.value))));
  }
}
