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
@JsonPropertyOrder({
  "always_off",
  "always_on",
  "composite/development",
  "jaeger_remote/development",
  "parent_based",
  "probability/development",
  "trace_id_ratio_based"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class SamplerModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("always_off")
  private AlwaysOffSamplerModel alwaysOff;

  /** (Can be null) */
  @Nullable
  @JsonProperty("always_on")
  private AlwaysOnSamplerModel alwaysOn;

  /** (Can be null) */
  @Nullable
  @JsonProperty("composite/development")
  private ExperimentalComposableSamplerModel compositeDevelopment;

  @Nullable
  @JsonProperty("jaeger_remote/development")
  private ExperimentalJaegerRemoteSamplerModel jaegerRemoteDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("parent_based")
  private ParentBasedSamplerModel parentBased;

  /** (Can be null) */
  @Nullable
  @JsonProperty("probability/development")
  private ExperimentalProbabilitySamplerModel probabilityDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("trace_id_ratio_based")
  private TraceIdRatioBasedSamplerModel traceIdRatioBased;

  @JsonIgnore
  private Map<String, SamplerPropertyModel> additionalProperties =
      new LinkedHashMap<String, SamplerPropertyModel>();

  @JsonProperty("always_off")
  @Nullable
  public AlwaysOffSamplerModel getAlwaysOff() {
    return alwaysOff;
  }

  public SamplerModel withAlwaysOff(AlwaysOffSamplerModel alwaysOff) {
    this.alwaysOff = alwaysOff;
    return this;
  }

  @JsonProperty("always_on")
  @Nullable
  public AlwaysOnSamplerModel getAlwaysOn() {
    return alwaysOn;
  }

  public SamplerModel withAlwaysOn(AlwaysOnSamplerModel alwaysOn) {
    this.alwaysOn = alwaysOn;
    return this;
  }

  @JsonProperty("composite/development")
  @Nullable
  public ExperimentalComposableSamplerModel getCompositeDevelopment() {
    return compositeDevelopment;
  }

  public SamplerModel withCompositeDevelopment(
      ExperimentalComposableSamplerModel compositeDevelopment) {
    this.compositeDevelopment = compositeDevelopment;
    return this;
  }

  @JsonProperty("jaeger_remote/development")
  @Nullable
  public ExperimentalJaegerRemoteSamplerModel getJaegerRemoteDevelopment() {
    return jaegerRemoteDevelopment;
  }

  public SamplerModel withJaegerRemoteDevelopment(
      ExperimentalJaegerRemoteSamplerModel jaegerRemoteDevelopment) {
    this.jaegerRemoteDevelopment = jaegerRemoteDevelopment;
    return this;
  }

  @JsonProperty("parent_based")
  @Nullable
  public ParentBasedSamplerModel getParentBased() {
    return parentBased;
  }

  public SamplerModel withParentBased(ParentBasedSamplerModel parentBased) {
    this.parentBased = parentBased;
    return this;
  }

  @JsonProperty("probability/development")
  @Nullable
  public ExperimentalProbabilitySamplerModel getProbabilityDevelopment() {
    return probabilityDevelopment;
  }

  public SamplerModel withProbabilityDevelopment(
      ExperimentalProbabilitySamplerModel probabilityDevelopment) {
    this.probabilityDevelopment = probabilityDevelopment;
    return this;
  }

  @JsonProperty("trace_id_ratio_based")
  @Nullable
  public TraceIdRatioBasedSamplerModel getTraceIdRatioBased() {
    return traceIdRatioBased;
  }

  public SamplerModel withTraceIdRatioBased(TraceIdRatioBasedSamplerModel traceIdRatioBased) {
    this.traceIdRatioBased = traceIdRatioBased;
    return this;
  }

  @JsonAnyGetter
  public Map<String, SamplerPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, SamplerPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public SamplerModel withAdditionalProperty(String name, SamplerPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(SamplerModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("alwaysOff");
    sb.append('=');
    sb.append(((this.alwaysOff == null) ? "<null>" : this.alwaysOff));
    sb.append(',');
    sb.append("alwaysOn");
    sb.append('=');
    sb.append(((this.alwaysOn == null) ? "<null>" : this.alwaysOn));
    sb.append(',');
    sb.append("compositeDevelopment");
    sb.append('=');
    sb.append(((this.compositeDevelopment == null) ? "<null>" : this.compositeDevelopment));
    sb.append(',');
    sb.append("jaegerRemoteDevelopment");
    sb.append('=');
    sb.append(((this.jaegerRemoteDevelopment == null) ? "<null>" : this.jaegerRemoteDevelopment));
    sb.append(',');
    sb.append("parentBased");
    sb.append('=');
    sb.append(((this.parentBased == null) ? "<null>" : this.parentBased));
    sb.append(',');
    sb.append("probabilityDevelopment");
    sb.append('=');
    sb.append(((this.probabilityDevelopment == null) ? "<null>" : this.probabilityDevelopment));
    sb.append(',');
    sb.append("traceIdRatioBased");
    sb.append('=');
    sb.append(((this.traceIdRatioBased == null) ? "<null>" : this.traceIdRatioBased));
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
    result =
        ((result * 31)
            + ((this.compositeDevelopment == null) ? 0 : this.compositeDevelopment.hashCode()));
    result =
        ((result * 31)
            + ((this.jaegerRemoteDevelopment == null)
                ? 0
                : this.jaegerRemoteDevelopment.hashCode()));
    result =
        ((result * 31)
            + ((this.probabilityDevelopment == null) ? 0 : this.probabilityDevelopment.hashCode()));
    result =
        ((result * 31)
            + ((this.traceIdRatioBased == null) ? 0 : this.traceIdRatioBased.hashCode()));
    result = ((result * 31) + ((this.alwaysOff == null) ? 0 : this.alwaysOff.hashCode()));
    result = ((result * 31) + ((this.parentBased == null) ? 0 : this.parentBased.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.alwaysOn == null) ? 0 : this.alwaysOn.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof SamplerModel) == false) {
      return false;
    }
    SamplerModel rhs = ((SamplerModel) other);
    return (((((((((this.compositeDevelopment == rhs.compositeDevelopment)
                                    || ((this.compositeDevelopment != null)
                                        && this.compositeDevelopment.equals(
                                            rhs.compositeDevelopment)))
                                && ((this.jaegerRemoteDevelopment == rhs.jaegerRemoteDevelopment)
                                    || ((this.jaegerRemoteDevelopment != null)
                                        && this.jaegerRemoteDevelopment.equals(
                                            rhs.jaegerRemoteDevelopment))))
                            && ((this.probabilityDevelopment == rhs.probabilityDevelopment)
                                || ((this.probabilityDevelopment != null)
                                    && this.probabilityDevelopment.equals(
                                        rhs.probabilityDevelopment))))
                        && ((this.traceIdRatioBased == rhs.traceIdRatioBased)
                            || ((this.traceIdRatioBased != null)
                                && this.traceIdRatioBased.equals(rhs.traceIdRatioBased))))
                    && ((this.alwaysOff == rhs.alwaysOff)
                        || ((this.alwaysOff != null) && this.alwaysOff.equals(rhs.alwaysOff))))
                && ((this.parentBased == rhs.parentBased)
                    || ((this.parentBased != null) && this.parentBased.equals(rhs.parentBased))))
            && ((this.additionalProperties == rhs.additionalProperties)
                || ((this.additionalProperties != null)
                    && this.additionalProperties.equals(rhs.additionalProperties))))
        && ((this.alwaysOn == rhs.alwaysOn)
            || ((this.alwaysOn != null) && this.alwaysOn.equals(rhs.alwaysOn))));
  }
}
