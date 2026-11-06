/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IdGeneratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalTracerConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalTracerConfiguratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalTracerMatcherAndConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.TracerProviderModelAccessor;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
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

    context.setInternalTelemetry(builder::setMeterProvider);

    SpanLimits spanLimits =
        SpanLimitsFactory.getInstance()
            .create(
                SpanLimitsAndAttributeLimits.create(
                    model.getAttributeLimits(), tracerProviderModel.getLimits()),
                context);
    builder.setSpanLimits(spanLimits);

    SamplerModel samplerModel = tracerProviderModel.getSampler();
    if (samplerModel != null) {
      Sampler sampler =
          context.withConfigPath(
              "sampler", () -> SamplerFactory.getInstance().create(samplerModel, context));
      builder.setSampler(sampler);
    }

    List<SpanProcessorModel> processors = tracerProviderModel.getProcessors();
    if (processors != null) {
      for (int i = 0; i < processors.size(); i++) {
        SpanProcessorModel processor = processors.get(i);
        builder.addSpanProcessor(
            context.withConfigPath(
                "processors[" + i + "]",
                () -> SpanProcessorFactory.getInstance().create(processor, context)));
      }
    }

    IdGeneratorModel idGeneratorModel = tracerProviderModel.getIdGenerator();
    if (idGeneratorModel != null) {
      builder.setIdGenerator(
          context.withConfigPath(
              "id_generator",
              () -> IdGeneratorFactory.getInstance().create(idGeneratorModel, context)));
    }

    ExperimentalTracerConfiguratorModel tracerConfiguratorModel =
        TracerProviderModelAccessor.getTracerConfigurator(tracerProviderModel);
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
        for (int i = 0; i < tracerMatcherAndConfigs.size(); i++) {
          ExperimentalTracerMatcherAndConfigModel tracerMatcherAndConfig =
              tracerMatcherAndConfigs.get(i);
          String name =
              context.withConfigPath(
                  "tracer_configurator/development.tracers[" + i + "]",
                  () -> requireNonNull(context, tracerMatcherAndConfig.getName(), "name"));
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
      if (model.getEnabled() != null && !model.getEnabled()) {
        return TracerConfig.disabled();
      }
      return TracerConfig.defaultConfig();
    }
  }
}
