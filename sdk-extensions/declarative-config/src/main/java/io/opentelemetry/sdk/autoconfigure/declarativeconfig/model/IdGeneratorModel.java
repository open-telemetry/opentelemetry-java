/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"random"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class IdGeneratorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("random")
  private RandomIdGeneratorModel random;

  @JsonIgnore
  private Map<String, IdGeneratorPropertyModel> additionalProperties =
      new LinkedHashMap<String, IdGeneratorPropertyModel>();

  @JsonProperty("random")
  @Nullable
  public RandomIdGeneratorModel getRandom() {
    return random;
  }

  public IdGeneratorModel withRandom(RandomIdGeneratorModel random) {
    this.random = random;
    return this;
  }

  @JsonAnyGetter
  public Map<String, IdGeneratorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, IdGeneratorPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public IdGeneratorModel withAdditionalProperty(String name, IdGeneratorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(IdGeneratorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("random");
    sb.append('=');
    sb.append(((this.random == null) ? "<null>" : this.random));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
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
    result = ((result * 31) + ((this.random == null) ? 0 : this.random.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof IdGeneratorModel) == false) {
      return false;
    }
    IdGeneratorModel rhs = ((IdGeneratorModel) other);
    return (((this.random == rhs.random)
            || ((this.random != null) && this.random.equals(rhs.random)))
        && ((this.additionalProperties == rhs.additionalProperties)
            || ((this.additionalProperties != null)
                && this.additionalProperties.equals(rhs.additionalProperties))));
  }
}
