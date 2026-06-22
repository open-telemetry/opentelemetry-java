/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "host",
  "port",
  "scope_info_enabled",
  "target_info_enabled/development",
  "resource_constant_labels",
  "translation_strategy"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalPrometheusMetricExporterModel {

  /**
   * Configure host. If omitted or null, localhost is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("host")
  @JsonPropertyDescription("Configure host.\nIf omitted or null, localhost is used.\n")
  private String host;

  /**
   * Configure port. If omitted or null, 9464 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("port")
  @JsonPropertyDescription("Configure port.\nIf omitted or null, 9464 is used.\n")
  private Integer port;

  /**
   * Configure Prometheus Exporter to produce metrics with scope labels. If omitted or null, true is
   * used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("scope_info_enabled")
  @JsonPropertyDescription(
      "Configure Prometheus Exporter to produce metrics with scope labels.\nIf omitted or null, true is used.\n")
  private Boolean scopeInfoEnabled;

  /**
   * Configure Prometheus Exporter to produce metrics with a target info metric for the resource. If
   * omitted or null, true is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("target_info_enabled/development")
  @JsonPropertyDescription(
      "Configure Prometheus Exporter to produce metrics with a target info metric for the resource.\nIf omitted or null, true is used.\n")
  private Boolean targetInfoEnabledDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("resource_constant_labels")
  private IncludeExcludeModel resourceConstantLabels;

  /** (Can be null) */
  @Nullable
  @JsonProperty("translation_strategy")
  private ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy
      translationStrategy;

  /** Configure host. If omitted or null, localhost is used. */
  @JsonProperty("host")
  @Nullable
  public String getHost() {
    return host;
  }

  public ExperimentalPrometheusMetricExporterModel withHost(String host) {
    this.host = host;
    return this;
  }

  /** Configure port. If omitted or null, 9464 is used. */
  @JsonProperty("port")
  @Nullable
  public Integer getPort() {
    return port;
  }

  public ExperimentalPrometheusMetricExporterModel withPort(Integer port) {
    this.port = port;
    return this;
  }

  /**
   * Configure Prometheus Exporter to produce metrics with scope labels. If omitted or null, true is
   * used.
   */
  @JsonProperty("scope_info_enabled")
  @Nullable
  public Boolean getScopeInfoEnabled() {
    return scopeInfoEnabled;
  }

  public ExperimentalPrometheusMetricExporterModel withScopeInfoEnabled(Boolean scopeInfoEnabled) {
    this.scopeInfoEnabled = scopeInfoEnabled;
    return this;
  }

  /**
   * Configure Prometheus Exporter to produce metrics with a target info metric for the resource. If
   * omitted or null, true is used.
   */
  @JsonProperty("target_info_enabled/development")
  @Nullable
  public Boolean getTargetInfoEnabledDevelopment() {
    return targetInfoEnabledDevelopment;
  }

  public ExperimentalPrometheusMetricExporterModel withTargetInfoEnabledDevelopment(
      Boolean targetInfoEnabledDevelopment) {
    this.targetInfoEnabledDevelopment = targetInfoEnabledDevelopment;
    return this;
  }

  @JsonProperty("resource_constant_labels")
  @Nullable
  public IncludeExcludeModel getResourceConstantLabels() {
    return resourceConstantLabels;
  }

  public ExperimentalPrometheusMetricExporterModel withResourceConstantLabels(
      IncludeExcludeModel resourceConstantLabels) {
    this.resourceConstantLabels = resourceConstantLabels;
    return this;
  }

  @JsonProperty("translation_strategy")
  @Nullable
  public ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy
      getTranslationStrategy() {
    return translationStrategy;
  }

  public ExperimentalPrometheusMetricExporterModel withTranslationStrategy(
      ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy
          translationStrategy) {
    this.translationStrategy = translationStrategy;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalPrometheusMetricExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("host");
    sb.append('=');
    sb.append(((this.host == null) ? "<null>" : this.host));
    sb.append(',');
    sb.append("port");
    sb.append('=');
    sb.append(((this.port == null) ? "<null>" : this.port));
    sb.append(',');
    sb.append("scopeInfoEnabled");
    sb.append('=');
    sb.append(((this.scopeInfoEnabled == null) ? "<null>" : this.scopeInfoEnabled));
    sb.append(',');
    sb.append("targetInfoEnabledDevelopment");
    sb.append('=');
    sb.append(
        ((this.targetInfoEnabledDevelopment == null)
            ? "<null>"
            : this.targetInfoEnabledDevelopment));
    sb.append(',');
    sb.append("resourceConstantLabels");
    sb.append('=');
    sb.append(((this.resourceConstantLabels == null) ? "<null>" : this.resourceConstantLabels));
    sb.append(',');
    sb.append("translationStrategy");
    sb.append('=');
    sb.append(((this.translationStrategy == null) ? "<null>" : this.translationStrategy));
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
    result = ((result * 31) + ((this.host == null) ? 0 : this.host.hashCode()));
    result =
        ((result * 31)
            + ((this.targetInfoEnabledDevelopment == null)
                ? 0
                : this.targetInfoEnabledDevelopment.hashCode()));
    result =
        ((result * 31)
            + ((this.resourceConstantLabels == null) ? 0 : this.resourceConstantLabels.hashCode()));
    result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
    result =
        ((result * 31)
            + ((this.translationStrategy == null) ? 0 : this.translationStrategy.hashCode()));
    result =
        ((result * 31) + ((this.scopeInfoEnabled == null) ? 0 : this.scopeInfoEnabled.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalPrometheusMetricExporterModel) == false) {
      return false;
    }
    ExperimentalPrometheusMetricExporterModel rhs =
        ((ExperimentalPrometheusMetricExporterModel) other);
    return (((((((this.host == rhs.host) || ((this.host != null) && this.host.equals(rhs.host)))
                        && ((this.targetInfoEnabledDevelopment == rhs.targetInfoEnabledDevelopment)
                            || ((this.targetInfoEnabledDevelopment != null)
                                && this.targetInfoEnabledDevelopment.equals(
                                    rhs.targetInfoEnabledDevelopment))))
                    && ((this.resourceConstantLabels == rhs.resourceConstantLabels)
                        || ((this.resourceConstantLabels != null)
                            && this.resourceConstantLabels.equals(rhs.resourceConstantLabels))))
                && ((this.port == rhs.port) || ((this.port != null) && this.port.equals(rhs.port))))
            && ((this.translationStrategy == rhs.translationStrategy)
                || ((this.translationStrategy != null)
                    && this.translationStrategy.equals(rhs.translationStrategy))))
        && ((this.scopeInfoEnabled == rhs.scopeInfoEnabled)
            || ((this.scopeInfoEnabled != null)
                && this.scopeInfoEnabled.equals(rhs.scopeInfoEnabled))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
  public enum ExperimentalPrometheusTranslationStrategy {
    UNDERSCORE_ESCAPING_WITH_SUFFIXES("underscore_escaping_with_suffixes"),
    UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES_DEVELOPMENT(
        "underscore_escaping_without_suffixes/development"),
    NO_UTF_8_ESCAPING_WITH_SUFFIXES_DEVELOPMENT("no_utf8_escaping_with_suffixes/development"),
    NO_TRANSLATION_DEVELOPMENT("no_translation/development");
    private final String value;
    private static final Map<
            String,
            ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy>
        CONSTANTS =
            new HashMap<
                String,
                ExperimentalPrometheusMetricExporterModel
                    .ExperimentalPrometheusTranslationStrategy>();

    static {
      for (ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy c :
          values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ExperimentalPrometheusTranslationStrategy(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static ExperimentalPrometheusMetricExporterModel
            .ExperimentalPrometheusTranslationStrategy
        fromValue(String value) {
      ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy constant =
          CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
