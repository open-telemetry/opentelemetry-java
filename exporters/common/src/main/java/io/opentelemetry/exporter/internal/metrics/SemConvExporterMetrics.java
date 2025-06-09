/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.internal.Signal;
import java.util.Collections;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class SemConvExporterMetrics implements ExporterMetrics {

  private static final Clock CLOCK = Clock.getDefault();

  private final Supplier<MeterProvider> meterProviderSupplier;
  private final Signal signal;
  private final ComponentId componentId;
  private final Attributes additionalAttributes;

  @Nullable private volatile LongUpDownCounter inflight = null;
  @Nullable private volatile LongCounter exported = null;
  @Nullable private volatile DoubleHistogram duration = null;
  @Nullable private volatile Attributes allAttributes = null;

  public SemConvExporterMetrics(
      Supplier<MeterProvider> meterProviderSupplier,
      Signal signal,
      ComponentId componentId,
      Attributes additionalAttributes) {
    this.meterProviderSupplier = meterProviderSupplier;
    this.componentId = componentId;
    this.signal = signal;
    this.additionalAttributes = additionalAttributes;
  }

  @Override
  public ExporterMetrics.Recording startRecordingExport(int itemCount) {
    return new Recording(itemCount);
  }

  private Meter meter() {
    MeterProvider meterProvider = meterProviderSupplier.get();
    if (meterProvider == null) {
      meterProvider = MeterProvider.noop();
    }
    return meterProvider.get("io.opentelemetry.exporters." + componentId.getTypeName());
  }

  private Attributes allAttributes() {
    // attributes are initialized lazily to trigger lazy initialization of the componentId
    Attributes allAttributes = this.allAttributes;
    if (allAttributes == null) {
      AttributesBuilder builder = Attributes.builder();
      builder.put(SemConvAttributes.OTEL_COMPONENT_TYPE, componentId.getTypeName());
      builder.put(SemConvAttributes.OTEL_COMPONENT_NAME, componentId.getComponentName());
      builder.putAll(additionalAttributes);
      allAttributes = builder.build();
      this.allAttributes = allAttributes;
    }
    return allAttributes;
  }

  private LongUpDownCounter inflight() {
    LongUpDownCounter inflight = this.inflight;
    if (inflight == null || isNoop(inflight)) {
      String unit = signal.getMetricUnit();
      inflight =
          meter()
              .upDownCounterBuilder(signal.getExporterMetricNamespace() + ".inflight")
              .setUnit("{" + unit + "}")
              .setDescription(
                  "The number of "
                      + unit
                      + "s which were passed to the exporter, but that have not been exported yet (neither successful, nor failed)")
              .build();
      this.inflight = inflight;
    }
    return inflight;
  }

  private LongCounter exported() {
    LongCounter exported = this.exported;
    if (exported == null || isNoop(exported)) {
      String unit = signal.getMetricUnit();
      exported =
          meter()
              .counterBuilder(signal.getExporterMetricNamespace() + ".exported")
              .setUnit("{" + unit + "}")
              .setDescription(
                  "The number of "
                      + unit
                      + "s for which the export has finished, either successful or failed")
              .build();
      this.exported = exported;
    }
    return exported;
  }

  private DoubleHistogram duration() {
    DoubleHistogram duration = this.duration;
    if (duration == null || isNoop(duration)) {
      duration =
          meter()
              .histogramBuilder("otel.sdk.exporter.operation.duration")
              .setUnit("s")
              .setDescription("The duration of exporting a batch of telemetry records")
              .setExplicitBucketBoundariesAdvice(Collections.emptyList())
              .build();
      this.duration = duration;
    }
    return duration;
  }

  private void incrementInflight(long count) {
    inflight().add(count, allAttributes());
  }

  private void decrementInflight(long count) {
    inflight().add(-count, allAttributes());
  }

  private void incrementExported(long count, @Nullable String errorType) {
    exported().add(count, getAttributesWithPotentialError(errorType, Attributes.empty()));
  }

  static boolean isNoop(Object instrument) {
    // This is a poor way to identify a Noop implementation, but the API doesn't provide a better
    // way. Perhaps we could add a common "Noop" interface to allow for an instanceof check?
    return instrument.getClass().getSimpleName().startsWith("Noop");
  }

  private Attributes getAttributesWithPotentialError(
      @Nullable String errorType, Attributes additionalAttributes) {
    Attributes attributes = allAttributes();
    boolean errorPresent = errorType != null && !errorType.isEmpty();
    if (errorPresent || !additionalAttributes.isEmpty()) {
      AttributesBuilder builder = attributes.toBuilder();
      if (errorPresent) {
        builder.put(SemConvAttributes.ERROR_TYPE, errorType);
      }
      attributes = builder.putAll(additionalAttributes).build();
    }
    return attributes;
  }

  private void recordDuration(
      double seconds, @Nullable String errorType, Attributes requestAttributes) {
    duration().record(seconds, getAttributesWithPotentialError(errorType, requestAttributes));
  }

  private class Recording extends ExporterMetrics.Recording {

    private final int itemCount;

    private final long startNanoTime;

    private Recording(int itemCount) {
      this.itemCount = itemCount;
      startNanoTime = CLOCK.nanoTime();
      incrementInflight(itemCount);
    }

    @Override
    protected void doFinish(@Nullable String errorType, Attributes requestAttributes) {
      decrementInflight(itemCount);
      incrementExported(itemCount, errorType);
      long durationNanos = CLOCK.nanoTime() - startNanoTime;
      recordDuration(durationNanos / 1_000_000_000.0, errorType, requestAttributes);
    }
  }
}
