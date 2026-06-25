/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

/**
 * A rule for ExperimentalComposableRuleBasedSampler. A rule can have multiple match conditions -
 * the sampler will be applied if all match. If no conditions are specified, the rule matches all
 * spans that reach it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"attribute_values", "attribute_patterns", "span_kinds", "parent", "sampler"})
@Generated("jsonschema2pojo")
public class ExperimentalComposableRuleBasedSamplerRuleModel {

  @JsonProperty("attribute_values")
  @Nullable
  private ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValues;

  @JsonProperty("attribute_patterns")
  @Nullable
  private ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatterns;

  /**
   * The span kinds to match. If the span's kind matches any of these, it matches. Values include: *
   * client: client, a client span. * consumer: consumer, a consumer span. * internal: internal, an
   * internal span. * producer: producer, a producer span. * server: server, a server span. If
   * omitted, ignore.
   */
  @JsonProperty("span_kinds")
  @JsonPropertyDescription(
      "The span kinds to match. If the span's kind matches any of these, it matches.\nValues include:\n* client: client, a client span.\n* consumer: consumer, a consumer span.\n* internal: internal, an internal span.\n* producer: producer, a producer span.\n* server: server, a server span.\nIf omitted, ignore.\n")
  @Nullable
  private List<SpanKind> spanKinds;

  /**
   * The parent span types to match. Values include: * local: local, a local parent. * none: none,
   * no parent, i.e., the trace root. * remote: remote, a remote parent. If omitted, ignore.
   */
  @JsonProperty("parent")
  @JsonPropertyDescription(
      "The parent span types to match.\nValues include:\n* local: local, a local parent.\n* none: none, no parent, i.e., the trace root.\n* remote: remote, a remote parent.\nIf omitted, ignore.\n")
  @Nullable
  private List<ExperimentalSpanParent> parent;

  /** (Required) */
  @JsonProperty("sampler")
  @Nullable
  private ExperimentalComposableSamplerModel sampler;

  @JsonProperty("attribute_values")
  @Nullable
  public ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel getAttributeValues() {
    return attributeValues;
  }

  public ExperimentalComposableRuleBasedSamplerRuleModel withAttributeValues(
      ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValues) {
    this.attributeValues = attributeValues;
    return this;
  }

  @JsonProperty("attribute_patterns")
  @Nullable
  public ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel getAttributePatterns() {
    return attributePatterns;
  }

  public ExperimentalComposableRuleBasedSamplerRuleModel withAttributePatterns(
      ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatterns) {
    this.attributePatterns = attributePatterns;
    return this;
  }

  /**
   * The span kinds to match. If the span's kind matches any of these, it matches. Values include: *
   * client: client, a client span. * consumer: consumer, a consumer span. * internal: internal, an
   * internal span. * producer: producer, a producer span. * server: server, a server span. If
   * omitted, ignore.
   */
  @JsonProperty("span_kinds")
  @Nullable
  public List<SpanKind> getSpanKinds() {
    return spanKinds;
  }

  public ExperimentalComposableRuleBasedSamplerRuleModel withSpanKinds(List<SpanKind> spanKinds) {
    this.spanKinds = spanKinds;
    return this;
  }

  /**
   * The parent span types to match. Values include: * local: local, a local parent. * none: none,
   * no parent, i.e., the trace root. * remote: remote, a remote parent. If omitted, ignore.
   */
  @JsonProperty("parent")
  @Nullable
  public List<ExperimentalSpanParent> getParent() {
    return parent;
  }

  public ExperimentalComposableRuleBasedSamplerRuleModel withParent(
      List<ExperimentalSpanParent> parent) {
    this.parent = parent;
    return this;
  }

  /** (Required) */
  @JsonProperty("sampler")
  @Nullable
  public ExperimentalComposableSamplerModel getSampler() {
    return sampler;
  }

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
