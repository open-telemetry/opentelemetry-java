/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalMeterMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewSelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewStreamModel;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.internal.MeterConfig;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import java.io.Closeable;
import java.util.List;

final class MeterProviderFactory implements Factory<MeterProviderModel, SdkMeterProviderBuilder> {

  private static final MeterProviderFactory INSTANCE = new MeterProviderFactory();

  private MeterProviderFactory() {}

  static MeterProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkMeterProviderBuilder create(
      MeterProviderModel model, SpiHelper spiHelper, List<Closeable> closeables) {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();

    List<MetricReaderModel> readerModels = model.getReaders();
    if (readerModels != null) {
      readerModels.forEach(
          readerModel -> {
            MetricReaderAndCardinalityLimits readerAndCardinalityLimits =
                MetricReaderFactory.getInstance().create(readerModel, spiHelper, closeables);
            CardinalityLimitSelector cardinalityLimits =
                readerAndCardinalityLimits.getCardinalityLimitsSelector();
            if (cardinalityLimits == null) {
              builder.registerMetricReader(readerAndCardinalityLimits.getMetricReader());
            } else {
              builder.registerMetricReader(
                  readerAndCardinalityLimits.getMetricReader(), cardinalityLimits);
            }
          });
    }

    List<ViewModel> viewModels = model.getViews();
    if (viewModels != null) {
      viewModels.forEach(
          viewModel -> {
            ViewSelectorModel selector = requireNonNull(viewModel.getSelector(), "view selector");
            ViewStreamModel stream = requireNonNull(viewModel.getStream(), "view stream");
            builder.registerView(
                InstrumentSelectorFactory.getInstance().create(selector, spiHelper, closeables),
                ViewFactory.getInstance().create(stream, spiHelper, closeables));
          });
    }

    ExperimentalMeterConfiguratorModel meterConfiguratorModel =
        model.getMeterConfiguratorDevelopment();
    if (meterConfiguratorModel != null) {
      ExperimentalMeterConfigModel defaultConfigModel = meterConfiguratorModel.getDefaultConfig();
      ScopeConfiguratorBuilder<MeterConfig> configuratorBuilder = ScopeConfigurator.builder();
      if (defaultConfigModel != null) {
        configuratorBuilder.setDefault(
            MeterConfigFactory.INSTANCE.create(defaultConfigModel, spiHelper, closeables));
      }
      List<ExperimentalMeterMatcherAndConfigModel> meterMatcherAndConfigs =
          meterConfiguratorModel.getMeters();
      if (meterMatcherAndConfigs != null) {
        for (ExperimentalMeterMatcherAndConfigModel meterMatcherAndConfig :
            meterMatcherAndConfigs) {
          String name = requireNonNull(meterMatcherAndConfig.getName(), "meter matcher name");
          ExperimentalMeterConfigModel config = meterMatcherAndConfig.getConfig();
          if (name == null || config == null) {
            continue;
          }
          configuratorBuilder.addCondition(
              ScopeConfiguratorBuilder.nameMatchesGlob(name),
              MeterConfigFactory.INSTANCE.create(config, spiHelper, closeables));
        }
      }
      SdkMeterProviderUtil.setMeterConfigurator(builder, configuratorBuilder.build());
    }

    return builder;
  }

  private static class MeterConfigFactory
      implements Factory<ExperimentalMeterConfigModel, MeterConfig> {

    private static final MeterConfigFactory INSTANCE = new MeterConfigFactory();

    @Override
    public MeterConfig create(
        ExperimentalMeterConfigModel model, SpiHelper spiHelper, List<Closeable> closeables) {
      if (model.getDisabled() != null && model.getDisabled()) {
        return MeterConfig.disabled();
      }
      return MeterConfig.defaultConfig();
    }
  }
}
