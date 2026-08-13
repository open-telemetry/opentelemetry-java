/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanKindModel;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"attribute_values", "attribute_patterns", "span_kinds", "parent", "sampler"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalComposableRuleBasedSamplerRuleModel {

  @Nullable private ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValues;

  @Nullable
  private ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatterns;

  @Nullable private List<SpanKindModel> spanKinds;
  @Nullable private List<ExperimentalSpanParentModel> parent;
  @Nullable private ExperimentalComposableSamplerModel sampler;

  /**
   * Values to match against a single attribute. Non-string attributes are matched using their
   * string representation:
   *
   * <p>for example, a value of "404" would match the http.response.status_code 404. For array
   * attributes, if any
   *
   * <p>item matches, it is considered a match.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("attribute_values")
  @Nullable
  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel getAttributeValues() {
    return attributeValues;
  }

  @JsonProperty("attribute_values")
  public ExperimentalComposableRuleBasedSamplerRuleModel withAttributeValues(
      ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValues) {
    this.attributeValues = attributeValues;
    return this;
  }

  /**
   * Patterns to match against a single attribute. Non-string attributes are matched using their
   * string representation:
   *
   * <p>for example, a pattern of "4*" would match any http.response.status_code in 400-499. For
   * array attributes, if any
   *
   * <p>item matches, it is considered a match.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("attribute_patterns")
  @Nullable
  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel getAttributePatterns() {
    return attributePatterns;
  }

  @JsonProperty("attribute_patterns")
  public ExperimentalComposableRuleBasedSamplerRuleModel withAttributePatterns(
      ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatterns) {
    this.attributePatterns = attributePatterns;
    return this;
  }

  /**
   * The span kinds to match. If the span's kind matches any of these, it matches.
   *
   * <p>Values include:
   *
   * <p>* client: client, a client span.
   *
   * <p>* consumer: consumer, a consumer span.
   *
   * <p>* internal: internal, an internal span.
   *
   * <p>* producer: producer, a producer span.
   *
   * <p>* server: server, a server span.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("span_kinds")
  @Nullable
  public List<SpanKindModel> getSpanKinds() {
    return spanKinds;
  }

  @JsonProperty("span_kinds")
  public ExperimentalComposableRuleBasedSamplerRuleModel withSpanKinds(
      List<SpanKindModel> spanKinds) {
    this.spanKinds = spanKinds;
    return this;
  }

  /**
   * The parent span types to match.
   *
   * <p>Values include:
   *
   * <p>* local: local, a local parent.
   *
   * <p>* none: none, no parent, i.e., the trace root.
   *
   * <p>* remote: remote, a remote parent.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("parent")
  @Nullable
  public List<ExperimentalSpanParentModel> getParent() {
    return parent;
  }

  @JsonProperty("parent")
  public ExperimentalComposableRuleBasedSamplerRuleModel withParent(
      List<ExperimentalSpanParentModel> parent) {
    this.parent = parent;
    return this;
  }

  /**
   * The sampler to use for matching spans.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("sampler")
  @Nullable
  public ExperimentalComposableSamplerModel getSampler() {
    return sampler;
  }

  @JsonProperty("sampler")
  public ExperimentalComposableRuleBasedSamplerRuleModel withSampler(
      ExperimentalComposableSamplerModel sampler) {
    this.sampler = sampler;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalComposableRuleBasedSamplerRuleModel{"
        + "attributeValues="
        + attributeValues
        + ", attributePatterns="
        + attributePatterns
        + ", spanKinds="
        + spanKinds
        + ", parent="
        + parent
        + ", sampler="
        + sampler
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributeValues == null) ? 0 : this.attributeValues.hashCode();
    h *= 1000003;
    h ^= (this.attributePatterns == null) ? 0 : this.attributePatterns.hashCode();
    h *= 1000003;
    h ^= (this.spanKinds == null) ? 0 : this.spanKinds.hashCode();
    h *= 1000003;
    h ^= (this.parent == null) ? 0 : this.parent.hashCode();
    h *= 1000003;
    h ^= (this.sampler == null) ? 0 : this.sampler.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalComposableRuleBasedSamplerRuleModel) {
      ExperimentalComposableRuleBasedSamplerRuleModel that =
          (ExperimentalComposableRuleBasedSamplerRuleModel) o;
      return (this.attributeValues == null
              ? that.attributeValues == null
              : this.attributeValues.equals(that.attributeValues))
          && (this.attributePatterns == null
              ? that.attributePatterns == null
              : this.attributePatterns.equals(that.attributePatterns))
          && (this.spanKinds == null
              ? that.spanKinds == null
              : this.spanKinds.equals(that.spanKinds))
          && (this.parent == null ? that.parent == null : this.parent.equals(that.parent))
          && (this.sampler == null ? that.sampler == null : this.sampler.equals(that.sampler));
    }
    return false;
  }
}
