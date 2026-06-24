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
@JsonPropertyOrder({"tracecontext", "baggage", "b3", "b3multi"})
@Generated("jsonschema2pojo")
public class TextMapPropagatorModel {

  @JsonProperty("tracecontext")
  @Nullable
  private TraceContextPropagatorModel tracecontext;

  @JsonProperty("baggage")
  @Nullable
  private BaggagePropagatorModel baggage;

  @JsonProperty("b3")
  @Nullable
  private B3PropagatorModel b3;

  @JsonProperty("b3multi")
  @Nullable
  private B3MultiPropagatorModel b3multi;

  @JsonIgnore
  private Map<String, TextMapPropagatorPropertyModel> additionalProperties =
      new LinkedHashMap<String, TextMapPropagatorPropertyModel>();

  @JsonProperty("tracecontext")
  @Nullable
  public TraceContextPropagatorModel getTracecontext() {
    return tracecontext;
  }

  public TextMapPropagatorModel withTracecontext(TraceContextPropagatorModel tracecontext) {
    this.tracecontext = tracecontext;
    return this;
  }

  @JsonProperty("baggage")
  @Nullable
  public BaggagePropagatorModel getBaggage() {
    return baggage;
  }

  public TextMapPropagatorModel withBaggage(BaggagePropagatorModel baggage) {
    this.baggage = baggage;
    return this;
  }

  @JsonProperty("b3")
  @Nullable
  public B3PropagatorModel getB3() {
    return b3;
  }

  public TextMapPropagatorModel withB3(B3PropagatorModel b3) {
    this.b3 = b3;
    return this;
  }

  @JsonProperty("b3multi")
  @Nullable
  public B3MultiPropagatorModel getB3multi() {
    return b3multi;
  }

  public TextMapPropagatorModel withB3multi(B3MultiPropagatorModel b3multi) {
    this.b3multi = b3multi;
    return this;
  }

  @JsonAnyGetter
  public Map<String, TextMapPropagatorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, TextMapPropagatorPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public TextMapPropagatorModel withAdditionalProperty(
      String name, TextMapPropagatorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "TextMapPropagatorModel{"
        + "tracecontext="
        + tracecontext
        + ", baggage="
        + baggage
        + ", b3="
        + b3
        + ", b3multi="
        + b3multi
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.tracecontext == null) ? 0 : this.tracecontext.hashCode();
    h *= 1000003;
    h ^= (this.baggage == null) ? 0 : this.baggage.hashCode();
    h *= 1000003;
    h ^= (this.b3 == null) ? 0 : this.b3.hashCode();
    h *= 1000003;
    h ^= (this.b3multi == null) ? 0 : this.b3multi.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TextMapPropagatorModel) {
      TextMapPropagatorModel that = (TextMapPropagatorModel) o;
      return (this.tracecontext == null
              ? that.tracecontext == null
              : this.tracecontext.equals(that.tracecontext))
          && (this.baggage == null ? that.baggage == null : this.baggage.equals(that.baggage))
          && (this.b3 == null ? that.b3 == null : this.b3.equals(that.b3))
          && (this.b3multi == null ? that.b3multi == null : this.b3multi.equals(that.b3multi))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
