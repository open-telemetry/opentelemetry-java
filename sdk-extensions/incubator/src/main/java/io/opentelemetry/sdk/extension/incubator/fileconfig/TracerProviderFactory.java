/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ScopeMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.util.List;

final class TracerProviderFactory
    implements Factory<TracerProviderAndAttributeLimits, SdkTracerProviderBuilder> {

  private static final TracerProviderFactory INSTANCE = new TracerProviderFactory();

  private TracerProviderFactory() {}

  static TracerProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkTracerProviderBuilder create(
      TracerProviderAndAttributeLimits model, SpiHelper spiHelper, List<Closeable> closeables) {
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
    TracerProviderModel tracerProviderModel = model.getTracerProvider();
    if (tracerProviderModel == null) {
      return builder;
    }

    SpanLimits spanLimits =
        SpanLimitsFactory.getInstance()
            .create(
                SpanLimitsAndAttributeLimits.create(
                    model.getAttributeLimits(), tracerProviderModel.getLimits()),
                spiHelper,
                closeables);
    builder.setSpanLimits(spanLimits);

    if (tracerProviderModel.getSampler() != null) {
      Sampler sampler =
          SamplerFactory.getInstance()
              .create(tracerProviderModel.getSampler(), spiHelper, closeables);
      builder.setSampler(sampler);
    }

    List<SpanProcessorModel> processors = tracerProviderModel.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addSpanProcessor(
                  SpanProcessorFactory.getInstance().create(processor, spiHelper, closeables)));
    }

    TracerConfiguratorModel tracerConfiguratorModel = tracerProviderModel.getTracerConfigurator();
    if (tracerConfiguratorModel != null) {
      TracerConfigModel defaultConfigModel = tracerConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<TracerConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            TracerConfigFactory.INSTANCE.create(defaultConfigModel, spiHelper, closeables));
      }
      List<ScopeMatcherAndConfigModel> scopeConfigs = tracerConfiguratorModel.getScopeConfigs();
      if (scopeConfigs != null) {
        for (ScopeMatcherAndConfigModel scopeMatcherAndConfigModel : scopeConfigs) {
          String name = scopeMatcherAndConfigModel.getName();
          TracerConfigModel config = scopeMatcherAndConfigModel.getConfig();
          if (name == null || config == null) {
            continue;
          }
          configuratorBuilder.addCondition(
              ScopeConfiguratorBuilder.nameMatchesGlob(name),
              TracerConfigFactory.INSTANCE.create(config, spiHelper, closeables));
        }
      }
      SdkTracerProviderUtil.setTracerConfigurator(builder, configuratorBuilder.build());
    }

    return builder;
  }

  private static class TracerConfigFactory implements Factory<TracerConfigModel, TracerConfig> {

    private static final TracerConfigFactory INSTANCE = new TracerConfigFactory();

    @Override
    public TracerConfig create(
        TracerConfigModel model, SpiHelper spiHelper, List<Closeable> closeables) {
      if (model.getEnabled() != null && !model.getEnabled()) {
        return TracerConfig.disabled();
      }
      return TracerConfig.defaultConfig();
    }
  }
}
