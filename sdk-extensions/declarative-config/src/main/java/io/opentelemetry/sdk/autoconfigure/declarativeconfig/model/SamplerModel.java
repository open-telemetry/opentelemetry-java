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
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalComposableSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalJaegerRemoteSamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalProbabilitySamplerModel;
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
public class SamplerModel {

  @JsonProperty("always_off")
  @Nullable
  private AlwaysOffSamplerModel alwaysOff;

  @JsonProperty("always_on")
  @Nullable
  private AlwaysOnSamplerModel alwaysOn;

  @JsonProperty("composite/development")
  @Nullable
  private ExperimentalComposableSamplerModel compositeDevelopment;

  @JsonProperty("jaeger_remote/development")
  @Nullable
  private ExperimentalJaegerRemoteSamplerModel jaegerRemoteDevelopment;

  @JsonProperty("parent_based")
  @Nullable
  private ParentBasedSamplerModel parentBased;

  @JsonProperty("probability/development")
  @Nullable
  private ExperimentalProbabilitySamplerModel probabilityDevelopment;

  @JsonProperty("trace_id_ratio_based")
  @Nullable
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
    return "SamplerModel{"
        + "alwaysOff="
        + alwaysOff
        + ", alwaysOn="
        + alwaysOn
        + ", compositeDevelopment="
        + compositeDevelopment
        + ", jaegerRemoteDevelopment="
        + jaegerRemoteDevelopment
        + ", parentBased="
        + parentBased
        + ", probabilityDevelopment="
        + probabilityDevelopment
        + ", traceIdRatioBased="
        + traceIdRatioBased
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.alwaysOff == null) ? 0 : this.alwaysOff.hashCode();
    h *= 1000003;
    h ^= (this.alwaysOn == null) ? 0 : this.alwaysOn.hashCode();
    h *= 1000003;
    h ^= (this.compositeDevelopment == null) ? 0 : this.compositeDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.jaegerRemoteDevelopment == null) ? 0 : this.jaegerRemoteDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.parentBased == null) ? 0 : this.parentBased.hashCode();
    h *= 1000003;
    h ^= (this.probabilityDevelopment == null) ? 0 : this.probabilityDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.traceIdRatioBased == null) ? 0 : this.traceIdRatioBased.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SamplerModel) {
      SamplerModel that = (SamplerModel) o;
      return (this.alwaysOff == null
              ? that.alwaysOff == null
              : this.alwaysOff.equals(that.alwaysOff))
          && (this.alwaysOn == null ? that.alwaysOn == null : this.alwaysOn.equals(that.alwaysOn))
          && (this.compositeDevelopment == null
              ? that.compositeDevelopment == null
              : this.compositeDevelopment.equals(that.compositeDevelopment))
          && (this.jaegerRemoteDevelopment == null
              ? that.jaegerRemoteDevelopment == null
              : this.jaegerRemoteDevelopment.equals(that.jaegerRemoteDevelopment))
          && (this.parentBased == null
              ? that.parentBased == null
              : this.parentBased.equals(that.parentBased))
          && (this.probabilityDevelopment == null
              ? that.probabilityDevelopment == null
              : this.probabilityDevelopment.equals(that.probabilityDevelopment))
          && (this.traceIdRatioBased == null
              ? that.traceIdRatioBased == null
              : this.traceIdRatioBased.equals(that.traceIdRatioBased))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
