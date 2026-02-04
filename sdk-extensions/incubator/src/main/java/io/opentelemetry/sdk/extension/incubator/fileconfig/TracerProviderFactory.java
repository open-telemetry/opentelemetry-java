/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
import io.opentelemetry.sdk.trace.samplers.Sampler;
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
      TracerProviderAndAttributeLimits model, DeclarativeConfigContext context) {
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
    TracerProviderModel tracerProviderModel = model.getTracerProvider();
    if (tracerProviderModel == null) {
      return builder;
    }

    MeterProvider meterProvider = context.getMeterProvider();
    if (meterProvider != null) {
      builder.setMeterProvider(() -> meterProvider);
    }

    SpanLimits spanLimits =
        SpanLimitsFactory.getInstance()
            .create(
                SpanLimitsAndAttributeLimits.create(
                    model.getAttributeLimits(), tracerProviderModel.getLimits()),
                context);
    builder.setSpanLimits(spanLimits);

    if (tracerProviderModel.getSampler() != null) {
      Sampler sampler =
          SamplerFactory.getInstance().create(tracerProviderModel.getSampler(), context);
      builder.setSampler(sampler);
    }

    List<SpanProcessorModel> processors = tracerProviderModel.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addSpanProcessor(
                  SpanProcessorFactory.getInstance().create(processor, context)));
    }

    ExperimentalTracerConfiguratorModel tracerConfiguratorModel =
        tracerProviderModel.getTracerConfiguratorDevelopment();
    if (tracerConfiguratorModel != null) {
      ExperimentalTracerConfigModel defaultConfigModel = tracerConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<TracerConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            TracerConfigFactory.INSTANCE.create(defaultConfigModel, context));
      }
      List<ExperimentalTracerMatcherAndConfigModel> tracerMatcherAndConfigs =
          tracerConfiguratorModel.getTracers();
      if (tracerMatcherAndConfigs != null) {
        for (ExperimentalTracerMatcherAndConfigModel tracerMatcherAndConfig :
            tracerMatcherAndConfigs) {
          String name = requireNonNull(tracerMatcherAndConfig.getName(), "tracer matcher name");
          ExperimentalTracerConfigModel config = tracerMatcherAndConfig.getConfig();
          if (name == null || config == null) {
            continue;
          }
          configuratorBuilder.addCondition(
              ScopeConfiguratorBuilder.nameMatchesGlob(name),
              TracerConfigFactory.INSTANCE.create(config, context));
        }
      }
      SdkTracerProviderUtil.setTracerConfigurator(builder, configuratorBuilder.build());
    }

    return builder;
  }

  private static class TracerConfigFactory
      implements Factory<ExperimentalTracerConfigModel, TracerConfig> {

    private static final TracerConfigFactory INSTANCE = new TracerConfigFactory();

    @Override
    public TracerConfig create(
        ExperimentalTracerConfigModel model, DeclarativeConfigContext context) {
      if (model.getDisabled() != null && model.getDisabled()) {
        return TracerConfig.disabled();
      }
      return TracerConfig.defaultConfig();
    }
  }
}
