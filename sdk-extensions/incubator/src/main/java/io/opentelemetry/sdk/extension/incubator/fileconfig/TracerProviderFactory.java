/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class TracerProviderFactory implements Factory<TracerProvider, SdkTracerProviderBuilder> {

  private static final TracerProviderFactory INSTANCE = new TracerProviderFactory();

  private TracerProviderFactory() {}

  static TracerProviderFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public SdkTracerProviderBuilder create(
      @Nullable TracerProvider model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return SdkTracerProvider.builder();
    }

    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();

    SpanLimits spanLimits =
        SpanLimitsFactory.getInstance().create(model.getLimits(), spiHelper, closeables);
    builder.setSpanLimits(spanLimits);

    List<SpanProcessor> processors = model.getProcessors();
    if (processors != null) {
      processors.forEach(
          processor ->
              builder.addSpanProcessor(
                  SpanProcessorFactory.getInstance().create(processor, spiHelper, closeables)));
    }

    return builder;
  }
}
