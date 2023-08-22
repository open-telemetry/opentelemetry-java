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

    List<MetricReader> readers = model.getReaders();
    if (readers != null) {
      readers.forEach(
          processor -> {
            io.opentelemetry.sdk.metrics.export.MetricReader metricReader =
                MetricReaderFactory.getInstance().create(processor, spiHelper, closeables);
            if (metricReader != null) {
              builder.registerMetricReader(metricReader);
            }
          });
    }

    List<View> views = model.getViews();
    if (views != null) {
      views.forEach(
          view ->
              builder.registerView(
                  InstrumentSelectorFactory.getInstance()
                      .create(view.getSelector(), spiHelper, closeables),
                  ViewFactory.getInstance().create(view.getStream(), spiHelper, closeables)));
    }

    return builder;
  }
}
