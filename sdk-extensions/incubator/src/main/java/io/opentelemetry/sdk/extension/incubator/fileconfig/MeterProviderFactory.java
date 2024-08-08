/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.View;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.Closeable;
import java.util.List;

final class MeterProviderFactory implements Factory<MeterProvider, SdkMeterProviderBuilder> {

  private static final MeterProviderFactory INSTANCE = new MeterProviderFactory();

  private MeterProviderFactory() {}

  static MeterProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkMeterProviderBuilder create(
      MeterProvider model, SpiHelper spiHelper, List<Closeable> closeables) {
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
          viewModel -> {
            Selector selector = requireNonNull(viewModel.getSelector(), "view selector");
            Stream stream = requireNonNull(viewModel.getStream(), "view stream");
            builder.registerView(
                InstrumentSelectorFactory.getInstance().create(selector, spiHelper, closeables),
                ViewFactory.getInstance().create(stream, spiHelper, closeables));
          });
    }

    return builder;
  }
}
