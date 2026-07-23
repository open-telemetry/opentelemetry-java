/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"random"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class IdGeneratorModel {

  @Nullable private RandomIdGeneratorModel random;
  private Map<String, IdGeneratorPropertyModel> additionalProperties =
      new LinkedHashMap<String, IdGeneratorPropertyModel>();

  /**
   * Configure the ID generator to randomly generate TraceIds and SpanIds (spec default).
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("random")
  @Nullable
  public RandomIdGeneratorModel getRandom() {
    return random;
  }

  @JsonProperty("random")
  public IdGeneratorModel withRandom(RandomIdGeneratorModel random) {
    this.random = random;
    return this;
  }

  @JsonAnyGetter
  public Map<String, IdGeneratorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public IdGeneratorModel withAdditionalProperty(String name, IdGeneratorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "IdGeneratorModel{"
        + "random="
        + random
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.random == null) ? 0 : this.random.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof IdGeneratorModel) {
      IdGeneratorModel that = (IdGeneratorModel) o;
      return (this.random == null ? that.random == null : this.random.equals(that.random))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
