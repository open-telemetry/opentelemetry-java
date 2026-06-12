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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A rule for ExperimentalComposableRuleBasedSampler. A rule can have multiple match conditions -
 * the sampler will be applied if all match. If no conditions are specified, the rule matches all
 * spans that reach it.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"attribute_values", "attribute_patterns", "span_kinds", "parent", "sampler"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalComposableRuleBasedSamplerRuleModel {

  @Nullable
  @JsonProperty("attribute_values")
  private ExperimentalComposableRuleBasedSamplerRuleAttributeValuesModel attributeValues;

  @Nullable
  @JsonProperty("attribute_patterns")
  private ExperimentalComposableRuleBasedSamplerRuleAttributePatternsModel attributePatterns;

  /**
   * The span kinds to match. If the span's kind matches any of these, it matches. Values include: *
   * client: client, a client span. * consumer: consumer, a consumer span. * internal: internal, an
   * internal span. * producer: producer, a producer span. * server: server, a server span. If
   * omitted, ignore.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("span_kinds")
  @JsonPropertyDescription(
      "The span kinds to match. If the span's kind matches any of these, it matches.\nValues include:\n* client: client, a client span.\n* consumer: consumer, a consumer span.\n* internal: internal, an internal span.\n* producer: producer, a producer span.\n* server: server, a server span.\nIf omitted, ignore.\n")
  private List<SpanKind> spanKinds;

  /**
   * The parent span types to match. Values include: * local: local, a local parent. * none: none,
   * no parent, i.e., the trace root. * remote: remote, a remote parent. If omitted, ignore.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("parent")
  @JsonPropertyDescription(
      "The parent span types to match.\nValues include:\n* local: local, a local parent.\n* none: none, no parent, i.e., the trace root.\n* remote: remote, a remote parent.\nIf omitted, ignore.\n")
  private List<ExperimentalSpanParent> parent;

  /** (Required) */
  @JsonProperty("sampler")
  @Nonnull
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalComposableRuleBasedSamplerRuleModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("attributeValues");
    sb.append('=');
    sb.append(((this.attributeValues == null) ? "<null>" : this.attributeValues));
    sb.append(',');
    sb.append("attributePatterns");
    sb.append('=');
    sb.append(((this.attributePatterns == null) ? "<null>" : this.attributePatterns));
    sb.append(',');
    sb.append("spanKinds");
    sb.append('=');
    sb.append(((this.spanKinds == null) ? "<null>" : this.spanKinds));
    sb.append(',');
    sb.append("parent");
    sb.append('=');
    sb.append(((this.parent == null) ? "<null>" : this.parent));
    sb.append(',');
    sb.append("sampler");
    sb.append('=');
    sb.append(((this.sampler == null) ? "<null>" : this.sampler));
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
        ((result * 31) + ((this.attributeValues == null) ? 0 : this.attributeValues.hashCode()));
    result = ((result * 31) + ((this.spanKinds == null) ? 0 : this.spanKinds.hashCode()));
    result = ((result * 31) + ((this.parent == null) ? 0 : this.parent.hashCode()));
    result =
        ((result * 31)
            + ((this.attributePatterns == null) ? 0 : this.attributePatterns.hashCode()));
    result = ((result * 31) + ((this.sampler == null) ? 0 : this.sampler.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalComposableRuleBasedSamplerRuleModel) == false) {
      return false;
    }
    ExperimentalComposableRuleBasedSamplerRuleModel rhs =
        ((ExperimentalComposableRuleBasedSamplerRuleModel) other);
    return ((((((this.attributeValues == rhs.attributeValues)
                        || ((this.attributeValues != null)
                            && this.attributeValues.equals(rhs.attributeValues)))
                    && ((this.spanKinds == rhs.spanKinds)
                        || ((this.spanKinds != null) && this.spanKinds.equals(rhs.spanKinds))))
                && ((this.parent == rhs.parent)
                    || ((this.parent != null) && this.parent.equals(rhs.parent))))
            && ((this.attributePatterns == rhs.attributePatterns)
                || ((this.attributePatterns != null)
                    && this.attributePatterns.equals(rhs.attributePatterns))))
        && ((this.sampler == rhs.sampler)
            || ((this.sampler != null) && this.sampler.equals(rhs.sampler))));
  }
}
