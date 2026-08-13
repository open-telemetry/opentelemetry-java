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
@JsonPropertyOrder({"key", "included", "excluded"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel {

  @Nullable private String key;
  @Nullable private List<String> included;
  @Nullable private List<String> excluded;

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

  @JsonProperty("key")
  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withKey(String key) {
    this.key = key;
    return this;
  }

  /**
   * Configure list of value patterns to include.
   *
   * <p>Matching is case-sensitive. Values are evaluated to match as follows:
   *
   * <p>* If the value exactly matches.
   *
   * <p>* If the value matches the wildcard pattern, where '?' matches any single character and '*'
   * matches any number of characters including none.
   *
   * <p>If omitted, all values are included.
   */
  @JsonProperty("included")
  @Nullable
  public List<String> getIncluded() {
    return included;
  }

  @JsonProperty("included")
  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withIncluded(
      List<String> included) {
    this.included = included;
    return this;
  }

  /**
   * Configure list of value patterns to exclude. Applies after .included (i.e. excluded has higher
   * priority than included).
   *
   * <p>Matching is case-sensitive. Values are evaluated to match as follows:
   *
   * <p>* If the value exactly matches.
   *
   * <p>* If the value matches the wildcard pattern, where '?' matches any single character and '*'
   * matches any number of characters including none.
   *
   * <p>If omitted, .included attributes are included.
   */
  @JsonProperty("excluded")
  @Nullable
  public List<String> getExcluded() {
    return excluded;
  }

  @JsonProperty("excluded")
  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel withExcluded(
      List<String> excluded) {
    this.excluded = excluded;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel{"
        + "key="
        + key
        + ", included="
        + included
        + ", excluded="
        + excluded
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.key == null) ? 0 : this.key.hashCode();
    h *= 1000003;
    h ^= (this.included == null) ? 0 : this.included.hashCode();
    h *= 1000003;
    h ^= (this.excluded == null) ? 0 : this.excluded.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel) {
      ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel that =
          (ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel) o;
      return (this.key == null ? that.key == null : this.key.equals(that.key))
          && (this.included == null ? that.included == null : this.included.equals(that.included))
          && (this.excluded == null ? that.excluded == null : this.excluded.equals(that.excluded));
    }
    return false;
  }
}
