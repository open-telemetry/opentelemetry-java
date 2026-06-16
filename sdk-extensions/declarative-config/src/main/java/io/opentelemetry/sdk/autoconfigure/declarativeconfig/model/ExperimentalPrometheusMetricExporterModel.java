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
  "without_scope_info",
  "without_target_info/development",
  "with_resource_constant_labels",
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
   * Configure Prometheus Exporter to produce metrics without scope labels. If omitted or null,
   * false is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("without_scope_info")
  @JsonPropertyDescription(
      "Configure Prometheus Exporter to produce metrics without scope labels.\nIf omitted or null, false is used.\n")
  private Boolean withoutScopeInfo;

  /**
   * Configure Prometheus Exporter to produce metrics without a target info metric for the resource.
   * If omitted or null, false is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("without_target_info/development")
  @JsonPropertyDescription(
      "Configure Prometheus Exporter to produce metrics without a target info metric for the resource.\nIf omitted or null, false is used.\n")
  private Boolean withoutTargetInfoDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("with_resource_constant_labels")
  private IncludeExcludeModel withResourceConstantLabels;

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
   * Configure Prometheus Exporter to produce metrics without scope labels. If omitted or null,
   * false is used.
   */
  @JsonProperty("without_scope_info")
  @Nullable
  public Boolean getWithoutScopeInfo() {
    return withoutScopeInfo;
  }

  public ExperimentalPrometheusMetricExporterModel withWithoutScopeInfo(Boolean withoutScopeInfo) {
    this.withoutScopeInfo = withoutScopeInfo;
    return this;
  }

  /**
   * Configure Prometheus Exporter to produce metrics without a target info metric for the resource.
   * If omitted or null, false is used.
   */
  @JsonProperty("without_target_info/development")
  @Nullable
  public Boolean getWithoutTargetInfoDevelopment() {
    return withoutTargetInfoDevelopment;
  }

  public ExperimentalPrometheusMetricExporterModel withWithoutTargetInfoDevelopment(
      Boolean withoutTargetInfoDevelopment) {
    this.withoutTargetInfoDevelopment = withoutTargetInfoDevelopment;
    return this;
  }

  @JsonProperty("with_resource_constant_labels")
  @Nullable
  public IncludeExcludeModel getWithResourceConstantLabels() {
    return withResourceConstantLabels;
  }

  public ExperimentalPrometheusMetricExporterModel withWithResourceConstantLabels(
      IncludeExcludeModel withResourceConstantLabels) {
    this.withResourceConstantLabels = withResourceConstantLabels;
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
    sb.append("withoutScopeInfo");
    sb.append('=');
    sb.append(((this.withoutScopeInfo == null) ? "<null>" : this.withoutScopeInfo));
    sb.append(',');
    sb.append("withoutTargetInfoDevelopment");
    sb.append('=');
    sb.append(
        ((this.withoutTargetInfoDevelopment == null)
            ? "<null>"
            : this.withoutTargetInfoDevelopment));
    sb.append(',');
    sb.append("withResourceConstantLabels");
    sb.append('=');
    sb.append(
        ((this.withResourceConstantLabels == null) ? "<null>" : this.withResourceConstantLabels));
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
        ((result * 31) + ((this.withoutScopeInfo == null) ? 0 : this.withoutScopeInfo.hashCode()));
    result = ((result * 31) + ((this.port == null) ? 0 : this.port.hashCode()));
    result =
        ((result * 31)
            + ((this.withoutTargetInfoDevelopment == null)
                ? 0
                : this.withoutTargetInfoDevelopment.hashCode()));
    result =
        ((result * 31)
            + ((this.withResourceConstantLabels == null)
                ? 0
                : this.withResourceConstantLabels.hashCode()));
    result =
        ((result * 31)
            + ((this.translationStrategy == null) ? 0 : this.translationStrategy.hashCode()));
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
                        && ((this.withoutScopeInfo == rhs.withoutScopeInfo)
                            || ((this.withoutScopeInfo != null)
                                && this.withoutScopeInfo.equals(rhs.withoutScopeInfo))))
                    && ((this.port == rhs.port)
                        || ((this.port != null) && this.port.equals(rhs.port))))
                && ((this.withoutTargetInfoDevelopment == rhs.withoutTargetInfoDevelopment)
                    || ((this.withoutTargetInfoDevelopment != null)
                        && this.withoutTargetInfoDevelopment.equals(
                            rhs.withoutTargetInfoDevelopment))))
            && ((this.withResourceConstantLabels == rhs.withResourceConstantLabels)
                || ((this.withResourceConstantLabels != null)
                    && this.withResourceConstantLabels.equals(rhs.withResourceConstantLabels))))
        && ((this.translationStrategy == rhs.translationStrategy)
            || ((this.translationStrategy != null)
                && this.translationStrategy.equals(rhs.translationStrategy))));
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
