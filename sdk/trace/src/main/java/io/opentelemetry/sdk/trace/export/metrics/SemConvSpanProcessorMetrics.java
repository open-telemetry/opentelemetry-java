package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.sdk.common.InternalTelemetrySchemaVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import javax.annotation.Nullable;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class SemConvSpanProcessorMetrics implements SpanProcessorMetrics {

  private final LongCounter processed;

  @Nullable
  private final ObservableLongUpDownCounter queueCapacity;
  @Nullable
  private final ObservableLongUpDownCounter queueSize;

  private SemConvSpanProcessorMetrics(
      Supplier<MeterProvider> meterProviderSupplier,
      ComponentId componentId,
      @Nullable LongSupplier queueSizeSupplier,
      @Nullable LongSupplier queueCapacitySupplier
  ) {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    Meter meter = meterProvider.get("io.opentelemetry.processor." + componentId.getTypeName())

    if (queueSizeSupplier != null) {
      queueSize = meter.upDownCounterBuilder("otel.sdk.processor.span.queue.size")
    }
  }

  @Override
  public void recordSpanProcessedSuccessfully() {

  }

  @Override
  public void recordSpanProcessingFailed(String errorType) {

  }

  @Override
  public void close() {
    if (queueCapacity != null) {
      queueCapacity.close();
    }
    if (queueSize != null) {
      queueSize.close();
    }
  }
}
