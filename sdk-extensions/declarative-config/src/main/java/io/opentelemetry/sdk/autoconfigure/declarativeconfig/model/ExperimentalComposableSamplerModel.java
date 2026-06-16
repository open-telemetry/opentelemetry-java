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
@JsonPropertyOrder({"always_off", "always_on", "parent_threshold", "probability", "rule_based"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableSamplerModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("always_off")
  private ExperimentalComposableAlwaysOffSamplerModel alwaysOff;

  /** (Can be null) */
  @Nullable
  @JsonProperty("always_on")
  private ExperimentalComposableAlwaysOnSamplerModel alwaysOn;

  @Nullable
  @JsonProperty("parent_threshold")
  private ExperimentalComposableParentThresholdSamplerModel parentThreshold;

  /** (Can be null) */
  @Nullable
  @JsonProperty("probability")
  private ExperimentalComposableProbabilitySamplerModel probability;

  /** (Can be null) */
  @Nullable
  @JsonProperty("rule_based")
  private ExperimentalComposableRuleBasedSamplerModel ruleBased;

  @JsonIgnore
  private Map<String, ExperimentalComposableSamplerPropertyModel> additionalProperties =
      new LinkedHashMap<String, ExperimentalComposableSamplerPropertyModel>();

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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableSamplerModel.class.getName())
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
    sb.append("parentThreshold");
    sb.append('=');
    sb.append(((this.parentThreshold == null) ? "<null>" : this.parentThreshold));
    sb.append(',');
    sb.append("probability");
    sb.append('=');
    sb.append(((this.probability == null) ? "<null>" : this.probability));
    sb.append(',');
    sb.append("ruleBased");
    sb.append('=');
    sb.append(((this.ruleBased == null) ? "<null>" : this.ruleBased));
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
    result = ((result * 31) + ((this.ruleBased == null) ? 0 : this.ruleBased.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.alwaysOn == null) ? 0 : this.alwaysOn.hashCode()));
    result =
        ((result * 31) + ((this.parentThreshold == null) ? 0 : this.parentThreshold.hashCode()));
    result = ((result * 31) + ((this.probability == null) ? 0 : this.probability.hashCode()));
    result = ((result * 31) + ((this.alwaysOff == null) ? 0 : this.alwaysOff.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableSamplerModel) == false) {
      return false;
    }
    ExperimentalComposableSamplerModel rhs = ((ExperimentalComposableSamplerModel) other);
    return (((((((this.ruleBased == rhs.ruleBased)
                            || ((this.ruleBased != null) && this.ruleBased.equals(rhs.ruleBased)))
                        && ((this.additionalProperties == rhs.additionalProperties)
                            || ((this.additionalProperties != null)
                                && this.additionalProperties.equals(rhs.additionalProperties))))
                    && ((this.alwaysOn == rhs.alwaysOn)
                        || ((this.alwaysOn != null) && this.alwaysOn.equals(rhs.alwaysOn))))
                && ((this.parentThreshold == rhs.parentThreshold)
                    || ((this.parentThreshold != null)
                        && this.parentThreshold.equals(rhs.parentThreshold))))
            && ((this.probability == rhs.probability)
                || ((this.probability != null) && this.probability.equals(rhs.probability))))
        && ((this.alwaysOff == rhs.alwaysOff)
            || ((this.alwaysOff != null) && this.alwaysOff.equals(rhs.alwaysOff))));
  }
}
