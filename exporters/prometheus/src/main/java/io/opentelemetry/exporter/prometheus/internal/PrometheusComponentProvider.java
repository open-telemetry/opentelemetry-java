/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.exporter.prometheus.TranslationStrategy;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.common.internal.IncludeExcludePredicate;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.List;

/**
 * Declarative configuration SPI implementation for {@link PrometheusHttpServer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class PrometheusComponentProvider implements ComponentProvider {

  @Override
  public Class<MetricReader> getType() {
    return MetricReader.class;
  }

  @Override
  public String getName() {
    return "prometheus/development";
  }

  @Override
  public MetricReader create(DeclarativeConfigProperties config) {
    PrometheusHttpServerBuilder prometheusBuilder = PrometheusHttpServer.builder();

    Integer port = config.getInt("port");
    if (port != null) {
      prometheusBuilder.setPort(port);
    }

    String host = config.getString("host");
    if (host != null) {
      prometheusBuilder.setHost(host);
    }

    Boolean withoutTargetInfo = config.getBoolean("without_target_info");
    if (withoutTargetInfo != null) {
      prometheusBuilder.setTargetInfoMetricEnabled(!withoutTargetInfo);
    }
    Boolean withoutScopeInfo = config.getBoolean("without_scope_info");
    if (withoutScopeInfo != null) {
      prometheusBuilder.setOtelScopeLabelsEnabled(!withoutScopeInfo);
    }
    String translationStrategy = config.getString("translation_strategy");
    if (translationStrategy != null) {
      prometheusBuilder.setTranslationStrategy(parseTranslationStrategy(translationStrategy));
    }

    DeclarativeConfigProperties withResourceConstantLabels =
        config.getStructured("with_resource_constant_labels");
    if (withResourceConstantLabels != null) {
      List<String> included = withResourceConstantLabels.getScalarList("included", String.class);
      List<String> excluded = withResourceConstantLabels.getScalarList("excluded", String.class);
      if (included != null && included.isEmpty()) {
        throw new DeclarativeConfigException("included must not be empty");
      }
      if (excluded != null && excluded.isEmpty()) {
        throw new DeclarativeConfigException("excluded must not be empty");
      }
      prometheusBuilder.setAllowedResourceAttributesFilter(
          IncludeExcludePredicate.createPatternMatching(included, excluded));
    }

    return prometheusBuilder.build();
  }

  private static TranslationStrategy parseTranslationStrategy(String value) {
    switch (value) {
      case "underscore_escaping_with_suffixes":
        return TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES;
      case "underscore_escaping_without_suffixes/development":
        return TranslationStrategy.UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES;
      case "no_utf8_escaping_with_suffixes/development":
        return TranslationStrategy.NO_UTF8_ESCAPING_WITH_SUFFIXES;
      case "no_translation/development":
        return TranslationStrategy.NO_TRANSLATION;
      default:
        throw new IllegalArgumentException("Unsupported translation_strategy: " + value);
    }
  }
}
