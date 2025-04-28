/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import java.util.Collections;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ExporterMetrics {

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public enum Signal {
    SPAN("span", "span"),
    METRIC("metric_data_point", "data_point"),
    LOG("log", "log_record");

    private final String namespace;
    private final String unit;

    Signal(String namespace, String unit) {
      this.namespace = namespace;
      this.unit = unit;
    }

    @Override
    public String toString() {
      return namespace;
    }
  }

  private static final Clock CLOCK = Clock.getDefault();

  private final Supplier<MeterProvider> meterProviderSupplier;
  private final Signal signal;
  private final ComponentId componentId;
  private final Attributes additionalAttributes;
  private final boolean enabled;

  @Nullable private volatile LongUpDownCounter inflight = null;
  @Nullable private volatile LongCounter exported = null;
  @Nullable private volatile DoubleHistogram duration = null;
  @Nullable private volatile Attributes allAttributes = null;

  public ExporterMetrics(
      HealthMetricLevel level,
      Supplier<MeterProvider> meterProviderSupplier,
      Signal signal,
      ComponentId componentId) {
    this(level, meterProviderSupplier, signal, componentId, null);
  }

  public ExporterMetrics(
      HealthMetricLevel level,
      Supplier<MeterProvider> meterProviderSupplier,
      Signal signal,
      ComponentId componentId,
      @Nullable Attributes additionalAttributes) {
    switch (level) {
      case ON:
        enabled = true;
        break;
      case OFF:
      case LEGACY:
        enabled = false;
        break;
      default:
        throw new IllegalArgumentException("Unhandled case " + level);
    }
    ;

    this.meterProviderSupplier = meterProviderSupplier;
    this.componentId = componentId;
    this.signal = signal;
    if (additionalAttributes != null) {
      this.additionalAttributes = additionalAttributes;
    } else {
      this.additionalAttributes = Attributes.empty();
    }
  }

  public Recording startRecordingExport(int itemCount) {
    return new Recording(itemCount);
  }

  private Meter meter() {
    return meterProviderSupplier
        .get()
        .get("io.opentelemetry.exporters." + componentId.getTypeName());
  }

  private Attributes allAttributes() {
    // attributes are initialized lazily to trigger lazy initialization of the componentId
    Attributes allAttributes = this.allAttributes;
    if (allAttributes == null) {
      AttributesBuilder builder = Attributes.builder();
      componentId.put(builder);
      builder.putAll(additionalAttributes);
      allAttributes = builder.build();
      this.allAttributes = allAttributes;
    }
    return allAttributes;
  }

  private LongUpDownCounter inflight() {
    LongUpDownCounter inflight = this.inflight;
    if (inflight == null || isNoop(inflight)) {
      inflight =
          meter()
              .upDownCounterBuilder("otel.sdk.exporter." + signal.namespace + ".inflight")
              .setUnit("{" + signal.unit + "}")
              .setDescription(
                  "The number of "
                      + signal.unit
                      + "s which were passed to the exporter, but that have not been exported yet (neither successful, nor failed)")
              .build();
      this.inflight = inflight;
    }
    return inflight;
  }

  private LongCounter exported() {
    LongCounter exported = this.exported;
    if (exported == null || isNoop(exported)) {
      exported =
          meter()
              .counterBuilder("otel.sdk.exporter." + signal.namespace + ".exported")
              .setUnit("{" + signal.unit + "}")
              .setDescription(
                  "The number of "
                      + signal.unit
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
    if (!enabled) {
      return;
    }
    inflight().add(count, allAttributes());
  }

  private void decrementInflight(long count) {
    if (!enabled) {
      return;
    }
    inflight().add(-count, allAttributes());
  }

  private void incrementExported(long count, @Nullable String errorType) {
    if (!enabled) {
      return;
    }
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
    if (!enabled) {
      return;
    }
    duration().record(seconds, getAttributesWithPotentialError(errorType, requestAttributes));
  }

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public class Recording {
    /** The number items (spans, log records or metric data points) being exported */
    private final int itemCount;

    private final long startNanoTime;

    private boolean alreadyEnded = false;

    private Recording(int itemCount) {
      this.itemCount = itemCount;
      startNanoTime = CLOCK.nanoTime();
      incrementInflight(itemCount);
    }

    public void finishSuccessful(Attributes requestAttributes) {
      finish(0, null, requestAttributes);
    }

    public void finishFailed(String errorReason, Attributes requestAttributes) {
      finish(itemCount, errorReason, requestAttributes);
    }

    private void finish(int failedCount, @Nullable String errorType, Attributes requestAttributes) {
      if (alreadyEnded) {
        throw new IllegalStateException("Recording already ended");
      }
      alreadyEnded = true;

      decrementInflight(itemCount);

      if (failedCount > 0) {
        if (errorType == null || errorType.isEmpty()) {
          throw new IllegalArgumentException(
              "Some items failed but no failure reason was provided");
        }
        incrementExported(failedCount, errorType);
      }
      int successCount = itemCount - failedCount;
      if (successCount > 0) {
        incrementExported(successCount, null);
      }
      long durationNanos = CLOCK.nanoTime() - startNanoTime;
      recordDuration(durationNanos / 1_000_000_000.0, errorType, requestAttributes);
    }
  }
}
