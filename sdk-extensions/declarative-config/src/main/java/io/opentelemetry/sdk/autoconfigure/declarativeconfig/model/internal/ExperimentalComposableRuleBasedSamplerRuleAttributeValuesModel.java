/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"key", "values"})
@Generated("jsonschema2pojo")
public class ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel {

  @JsonProperty("key")
  @Nullable
  private String key;

  @JsonProperty("values")
  @Nullable
  private List<String> values;

  /**
   * The attribute key to match against.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("key")
  @Nullable
  public String getKey() {
    return key;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel withKey(String key) {
    this.key = key;
    return this;
  }

  /**
   * The attribute values to match against. If the attribute's value matches any of these, it
   * matches.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("values")
  @Nullable
  public List<String> getValues() {
    return values;
  }

  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel withValues(
      List<String> values) {
    this.values = values;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel{"
        + "key="
        + key
        + ", values="
        + values
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.key == null) ? 0 : this.key.hashCode();
    h *= 1000003;
    h ^= (this.values == null) ? 0 : this.values.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel) {
      ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel that =
          (ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel) o;
      return (this.key == null ? that.key == null : this.key.equals(that.key))
          && (this.values == null ? that.values == null : this.values.equals(that.values));
    }
    return false;
  }
}
