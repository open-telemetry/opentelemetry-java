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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class TextMapPropagatorModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("tracecontext")
  private TraceContextPropagatorModel tracecontext;

  /** (Can be null) */
  @Nullable
  @JsonProperty("baggage")
  private BaggagePropagatorModel baggage;

  /** (Can be null) */
  @Nullable
  @JsonProperty("b3")
  private B3PropagatorModel b3;

  /** (Can be null) */
  @Nullable
  @JsonProperty("b3multi")
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
    StringBuilder sb = new StringBuilder();
    sb.append(TextMapPropagatorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("tracecontext");
    sb.append('=');
    sb.append(((this.tracecontext == null) ? "<null>" : this.tracecontext));
    sb.append(',');
    sb.append("baggage");
    sb.append('=');
    sb.append(((this.baggage == null) ? "<null>" : this.baggage));
    sb.append(',');
    sb.append("b3");
    sb.append('=');
    sb.append(((this.b3 == null) ? "<null>" : this.b3));
    sb.append(',');
    sb.append("b3multi");
    sb.append('=');
    sb.append(((this.b3multi == null) ? "<null>" : this.b3multi));
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
    result = ((result * 31) + ((this.b3multi == null) ? 0 : this.b3multi.hashCode()));
    result = ((result * 31) + ((this.b3 == null) ? 0 : this.b3.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.baggage == null) ? 0 : this.baggage.hashCode()));
    result = ((result * 31) + ((this.tracecontext == null) ? 0 : this.tracecontext.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof TextMapPropagatorModel) == false) {
      return false;
    }
    TextMapPropagatorModel rhs = ((TextMapPropagatorModel) other);
    return ((((((this.b3multi == rhs.b3multi)
                        || ((this.b3multi != null) && this.b3multi.equals(rhs.b3multi)))
                    && ((this.b3 == rhs.b3) || ((this.b3 != null) && this.b3.equals(rhs.b3))))
                && ((this.additionalProperties == rhs.additionalProperties)
                    || ((this.additionalProperties != null)
                        && this.additionalProperties.equals(rhs.additionalProperties))))
            && ((this.baggage == rhs.baggage)
                || ((this.baggage != null) && this.baggage.equals(rhs.baggage))))
        && ((this.tracecontext == rhs.tracecontext)
            || ((this.tracecontext != null) && this.tracecontext.equals(rhs.tracecontext))));
  }
}
