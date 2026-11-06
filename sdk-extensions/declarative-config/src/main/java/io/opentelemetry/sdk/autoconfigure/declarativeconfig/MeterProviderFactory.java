/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExemplarFilterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MetricReaderModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewSelectorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewStreamModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfiguratorModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterMatcherAndConfigModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.MeterProviderModelAccessor;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import java.util.List;

final class MeterProviderFactory implements Factory<MeterProviderModel, SdkMeterProviderBuilder> {

  private static final MeterProviderFactory INSTANCE = new MeterProviderFactory();

  private MeterProviderFactory() {}

  static MeterProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkMeterProviderBuilder create(
      MeterProviderModel model, DeclarativeConfigContext context) {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();

    List<MetricReaderModel> readerModels = model.getReaders();
    if (readerModels != null) {
      for (int i = 0; i < readerModels.size(); i++) {
        MetricReaderModel readerModel = readerModels.get(i);
        MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
            context.withConfigPath(
                "readers[" + i + "]",
                () -> MetricReaderFactory.getInstance().create(readerModel, context));
        CardinalityLimitSelector cardinalityLimits =
            readerAndCardinalityLimits.getCardinalityLimitsSelector();
        if (cardinalityLimits == null) {
          builder.registerMetricReader(readerAndCardinalityLimits.getMetricReader());
        } else {
          builder.registerMetricReader(
              readerAndCardinalityLimits.getMetricReader(), cardinalityLimits);
        }
      }
    }

    List<ViewModel> viewModels = model.getViews();
    if (viewModels != null) {
      for (int i = 0; i < viewModels.size(); i++) {
        ViewModel viewModel = viewModels.get(i);
        context.pushConfigPath("views[" + i + "]");
        try {
          ViewSelectorModel selector = requireNonNull(context, viewModel.getSelector(), "selector");
          ViewStreamModel stream = requireNonNull(context, viewModel.getStream(), "stream");
          builder.registerView(
              InstrumentSelectorFactory.getInstance().create(selector, context),
              ViewFactory.getInstance().create(stream, context));
        } finally {
          context.popConfigPath();
        }
      }
    }

    ExperimentalMeterConfiguratorModel meterConfiguratorModel =
        MeterProviderModelAccessor.getMeterConfigurator(model);
    if (meterConfiguratorModel != null) {
      ExperimentalMeterConfigModel defaultConfigModel = meterConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<MeterConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            MeterConfigFactory.INSTANCE.create(defaultConfigModel, context));
      }
      List<ExperimentalMeterMatcherAndConfigModel> meterMatcherAndConfigs =
          meterConfiguratorModel.getMeters();
      if (meterMatcherAndConfigs != null) {
        for (int i = 0; i < meterMatcherAndConfigs.size(); i++) {
          ExperimentalMeterMatcherAndConfigModel meterMatcherAndConfig =
              meterMatcherAndConfigs.get(i);
          String name =
              context.withConfigPath(
                  "meter_configurator/development.meters[" + i + "]",
                  () -> requireNonNull(context, meterMatcherAndConfig.getName(), "name"));
          ExperimentalMeterConfigModel config = meterMatcherAndConfig.getConfig();
          if (name == null || config == null) {
            continue;
          }
          configuratorBuilder.addCondition(
              ScopeConfiguratorBuilder.nameMatchesGlob(name),
              MeterConfigFactory.INSTANCE.create(config, context));
        }
      }
      SdkMeterProviderUtil.setMeterConfigurator(builder, configuratorBuilder.build());
    }

    ExemplarFilterModel exemplarFilterModel = model.getExemplarFilter();
    if (exemplarFilterModel != null) {
      builder.setExemplarFilter(
          ExemplarFilterFactory.getInstance().create(exemplarFilterModel, context));
    }

    return builder;
  }

  private static class MeterConfigFactory
      implements Factory<ExperimentalMeterConfigModel, MeterConfig> {

    private static final MeterConfigFactory INSTANCE = new MeterConfigFactory();

    @Override
    public MeterConfig create(
        ExperimentalMeterConfigModel model, DeclarativeConfigContext context) {
      if (model.getEnabled() != null && !model.getEnabled()) {
        return MeterConfig.disabled();
      }
      return MeterConfig.defaultConfig();
    }
  }
}
