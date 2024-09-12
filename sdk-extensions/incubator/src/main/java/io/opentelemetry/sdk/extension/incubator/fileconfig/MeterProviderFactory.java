/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigUtil.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReaderModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SelectorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.StreamModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ViewModel;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
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
            io.opentelemetry.sdk.metrics.export.MetricReader metricReader =
                MetricReaderFactory.getInstance().create(readerModel, spiHelper, closeables);
            if (metricReader != null) {
              builder.registerMetricReader(metricReader);
            }
          });
    }

    List<ViewModel> viewModels = model.getViews();
    if (viewModels != null) {
      viewModels.forEach(
          viewModel -> {
            SelectorModel selector = requireNonNull(viewModel.getSelector(), "view selector");
            StreamModel stream = requireNonNull(viewModel.getStream(), "view stream");
            builder.registerView(
                InstrumentSelectorFactory.getInstance().create(selector, spiHelper, closeables),
                ViewFactory.getInstance().create(stream, spiHelper, closeables));
          });
    }

    return builder;
  }
}
