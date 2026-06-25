/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ExperimentalPrometheusMetricExporterModel {

  @JsonProperty("host")
  @Nullable
  private String host;

  @JsonProperty("port")
  @Nullable
  private Integer port;

  @JsonProperty("scope_info_enabled")
  @Nullable
  private Boolean scopeInfoEnabled;

  @JsonProperty("target_info_enabled/development")
  @Nullable
  private Boolean targetInfoEnabledDevelopment;

  @JsonProperty("resource_constant_labels")
  @Nullable
  private IncludeExcludeModel resourceConstantLabels;

  @JsonProperty("translation_strategy")
  @Nullable
  private ExperimentalPrometheusMetricExporterModel.ExperimentalPrometheusTranslationStrategy
      translationStrategy;

  /**
   * Configure host.
   *
   * <p>If omitted or null, localhost is used.
   */
  @JsonProperty("host")
  @Nullable
  public String getHost() {
    return host;
  }

  public ExperimentalPrometheusMetricExporterModel withHost(String host) {
    this.host = host;
    return this;
  }

  /**
   * Configure port.
   *
   * <p>If omitted or null, 9464 is used.
   */
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
   * Configure Prometheus Exporter to produce metrics with scope labels.
   *
   * <p>If omitted or null, true is used.
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
   * Configure Prometheus Exporter to produce metrics with a target info metric for the resource.
   *
   * <p>If omitted or null, true is used.
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

  /**
   * Configure Prometheus Exporter to add resource attributes as metrics attributes, where the
   * resource attribute keys match the patterns.
   *
   * <p>If omitted, no resource attributes are added.
   */
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

  /**
   * Configure how metric names are translated to Prometheus metric names.
   *
   * <p>Values include:
   *
   * <p>* no_translation/development: Special character escaping is disabled. Type and unit suffixes
   * are disabled. Metric names are unaltered.
   *
   * <p>* no_utf8_escaping_with_suffixes/development: Special character escaping is disabled. Type
   * and unit suffixes are enabled.
   *
   * <p>* underscore_escaping_with_suffixes: Special character escaping is enabled. Type and unit
   * suffixes are enabled.
   *
   * <p>* underscore_escaping_without_suffixes/development: Special character escaping is enabled.
   * Type and unit suffixes are disabled. This represents classic Prometheus metric name
   * compatibility.
   *
   * <p>If omitted, underscore_escaping_with_suffixes is used.
   */
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
    return "ExperimentalPrometheusMetricExporterModel{"
        + "host="
        + host
        + ", port="
        + port
        + ", scopeInfoEnabled="
        + scopeInfoEnabled
        + ", targetInfoEnabledDevelopment="
        + targetInfoEnabledDevelopment
        + ", resourceConstantLabels="
        + resourceConstantLabels
        + ", translationStrategy="
        + translationStrategy
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.host == null) ? 0 : this.host.hashCode();
    h *= 1000003;
    h ^= (this.port == null) ? 0 : this.port.hashCode();
    h *= 1000003;
    h ^= (this.scopeInfoEnabled == null) ? 0 : this.scopeInfoEnabled.hashCode();
    h *= 1000003;
    h ^=
        (this.targetInfoEnabledDevelopment == null)
            ? 0
            : this.targetInfoEnabledDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.resourceConstantLabels == null) ? 0 : this.resourceConstantLabels.hashCode();
    h *= 1000003;
    h ^= (this.translationStrategy == null) ? 0 : this.translationStrategy.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalPrometheusMetricExporterModel) {
      ExperimentalPrometheusMetricExporterModel that =
          (ExperimentalPrometheusMetricExporterModel) o;
      return (this.host == null ? that.host == null : this.host.equals(that.host))
          && (this.port == null ? that.port == null : this.port.equals(that.port))
          && (this.scopeInfoEnabled == null
              ? that.scopeInfoEnabled == null
              : this.scopeInfoEnabled.equals(that.scopeInfoEnabled))
          && (this.targetInfoEnabledDevelopment == null
              ? that.targetInfoEnabledDevelopment == null
              : this.targetInfoEnabledDevelopment.equals(that.targetInfoEnabledDevelopment))
          && (this.resourceConstantLabels == null
              ? that.resourceConstantLabels == null
              : this.resourceConstantLabels.equals(that.resourceConstantLabels))
          && (this.translationStrategy == null
              ? that.translationStrategy == null
              : this.translationStrategy.equals(that.translationStrategy));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
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
