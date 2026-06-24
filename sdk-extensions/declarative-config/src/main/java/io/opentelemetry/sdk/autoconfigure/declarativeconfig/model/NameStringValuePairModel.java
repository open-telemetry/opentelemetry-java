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
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"name", "value"})
@Generated("jsonschema2pojo")
public class NameStringValuePairModel {

  /**
   * The name of the pair. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("name")
  @JsonPropertyDescription("The name of the pair.\nProperty is required and must be non-null.\n")
  @Nullable
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
  @Nullable
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
    return "NameStringValuePairModel{" + "name=" + name + ", value=" + value + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.name == null) ? 0 : this.name.hashCode();
    h *= 1000003;
    h ^= (this.value == null) ? 0 : this.value.hashCode();
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
          && (this.value == null ? that.value == null : this.value.equals(that.value));
    }
    return false;
  }
}
