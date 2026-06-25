/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

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
@JsonPropertyOrder({"always_off", "always_on", "parent_threshold", "probability", "rule_based"})
@Generated("jsonschema2pojo")
public class ExperimentalComposableSamplerModel {

  @JsonProperty("always_off")
  @Nullable
  private ExperimentalComposableAlwaysOffSamplerModel alwaysOff;

  @JsonProperty("always_on")
  @Nullable
  private ExperimentalComposableAlwaysOnSamplerModel alwaysOn;

  @JsonProperty("parent_threshold")
  @Nullable
  private ExperimentalComposableParentThresholdSamplerModel parentThreshold;

  @JsonProperty("probability")
  @Nullable
  private ExperimentalComposableProbabilitySamplerModel probability;

  @JsonProperty("rule_based")
  @Nullable
  private ExperimentalComposableRuleBasedSamplerModel ruleBased;

  @JsonIgnore
  private Map<String, ExperimentalComposableSamplerPropertyModel> additionalProperties =
      new LinkedHashMap<String, ExperimentalComposableSamplerPropertyModel>();

  /**
   * Configure sampler to be always_off.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("always_off")
  @Nullable
  public ExperimentalComposableAlwaysOffSamplerModel getAlwaysOff() {
    return alwaysOff;
  }

  public ExperimentalComposableSamplerModel withAlwaysOff(
      ExperimentalComposableAlwaysOffSamplerModel alwaysOff) {
    this.alwaysOff = alwaysOff;
    return this;
  }

  /**
   * Configure sampler to be always_on.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("always_on")
  @Nullable
  public ExperimentalComposableAlwaysOnSamplerModel getAlwaysOn() {
    return alwaysOn;
  }

  public ExperimentalComposableSamplerModel withAlwaysOn(
      ExperimentalComposableAlwaysOnSamplerModel alwaysOn) {
    this.alwaysOn = alwaysOn;
    return this;
  }

  /**
   * Configure sampler to be parent_threshold.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("parent_threshold")
  @Nullable
  public ExperimentalComposableParentThresholdSamplerModel getParentThreshold() {
    return parentThreshold;
  }

  public ExperimentalComposableSamplerModel withParentThreshold(
      ExperimentalComposableParentThresholdSamplerModel parentThreshold) {
    this.parentThreshold = parentThreshold;
    return this;
  }

  /**
   * Configure sampler to be probability.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("probability")
  @Nullable
  public ExperimentalComposableProbabilitySamplerModel getProbability() {
    return probability;
  }

  public ExperimentalComposableSamplerModel withProbability(
      ExperimentalComposableProbabilitySamplerModel probability) {
    this.probability = probability;
    return this;
  }

  /**
   * Configure sampler to be rule_based.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("rule_based")
  @Nullable
  public ExperimentalComposableRuleBasedSamplerModel getRuleBased() {
    return ruleBased;
  }

  public ExperimentalComposableSamplerModel withRuleBased(
      ExperimentalComposableRuleBasedSamplerModel ruleBased) {
    this.ruleBased = ruleBased;
    return this;
  }

  @JsonAnyGetter
  public Map<String, ExperimentalComposableSamplerPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, ExperimentalComposableSamplerPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public ExperimentalComposableSamplerModel withAdditionalProperty(
      String name, ExperimentalComposableSamplerPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableSamplerModel{"
        + "alwaysOff="
        + alwaysOff
        + ", alwaysOn="
        + alwaysOn
        + ", parentThreshold="
        + parentThreshold
        + ", probability="
        + probability
        + ", ruleBased="
        + ruleBased
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
    h ^= (this.parentThreshold == null) ? 0 : this.parentThreshold.hashCode();
    h *= 1000003;
    h ^= (this.probability == null) ? 0 : this.probability.hashCode();
    h *= 1000003;
    h ^= (this.ruleBased == null) ? 0 : this.ruleBased.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableSamplerModel) {
      ExperimentalComposableSamplerModel that = (ExperimentalComposableSamplerModel) o;
      return (this.alwaysOff == null
              ? that.alwaysOff == null
              : this.alwaysOff.equals(that.alwaysOff))
          && (this.alwaysOn == null ? that.alwaysOn == null : this.alwaysOn.equals(that.alwaysOn))
          && (this.parentThreshold == null
              ? that.parentThreshold == null
              : this.parentThreshold.equals(that.parentThreshold))
          && (this.probability == null
              ? that.probability == null
              : this.probability.equals(that.probability))
          && (this.ruleBased == null
              ? that.ruleBased == null
              : this.ruleBased.equals(that.ruleBased))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
