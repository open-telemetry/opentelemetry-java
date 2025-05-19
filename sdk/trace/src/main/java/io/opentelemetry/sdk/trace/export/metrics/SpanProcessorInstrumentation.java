package io.opentelemetry.sdk.trace.export.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetrySchemaVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import javax.annotation.Nullable;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class SpanProcessorInstrumentation implements AutoCloseable {

  private SpanProcessorInstrumentation(
      InternalTelemetrySchemaVersion schema,
      Supplier<MeterProvider> meterProviderSupplier,
      ComponentId componentId,
      @Nullable LongSupplier queueSizeSupplier,
      @Nullable LongSupplier queueCapacitySupplier
  ) {

  }

  @Override
  public void close() {
  }
}
