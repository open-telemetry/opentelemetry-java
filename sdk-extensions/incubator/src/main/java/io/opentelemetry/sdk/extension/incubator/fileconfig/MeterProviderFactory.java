/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.View;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class MeterProviderFactory implements Factory<MeterProvider, SdkMeterProviderBuilder> {

  private static final MeterProviderFactory INSTANCE = new MeterProviderFactory();

  private MeterProviderFactory() {}

  static MeterProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkMeterProviderBuilder create(
      @Nullable MeterProvider model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return SdkMeterProvider.builder();
    }

    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();

    List<MetricReader> readerModels = model.getReaders();
    if (readerModels != null) {
      readerModels.forEach(
          readerModel -> {
            io.opentelemetry.sdk.metrics.export.MetricReader metricReader =
                MetricReaderFactory.getInstance().create(readerModel, spiHelper, closeables);
            if (metricReader != null) {
              builder.registerMetricReader(metricReader);
            }
          });
    }

    List<View> viewModels = model.getViews();
    if (viewModels != null) {
      viewModels.forEach(
          viewModel ->
              builder.registerView(
                  InstrumentSelectorFactory.getInstance()
                      .create(viewModel.getSelector(), spiHelper, closeables),
                  ViewFactory.getInstance().create(viewModel.getStream(), spiHelper, closeables)));
    }

    return builder;
  }
}
