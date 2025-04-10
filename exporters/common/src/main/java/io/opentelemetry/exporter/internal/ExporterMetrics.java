/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class ExporterMetrics {

  // TODO: add semconv test
  private static final AttributeKey<String> ERROR_TYPE_ATTRIB =
      AttributeKey.stringKey("error.type");

  public enum Signal {
    SPAN("span", "span"),
    METRIC("metric", "data_point"),
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

  private final Supplier<MeterProvider> meterProviderSupplier;
  private final Signal signal;
  private final ComponentId componentId;
  private final Attributes additionalAttributes;
  private final boolean enabled;

  @Nullable private volatile LongUpDownCounter inflight = null;
  private volatile LongCounter exported = null;
  private volatile Attributes allAttributes = null;

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
    if (inflight == null) {
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
    if (exported == null) {
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

  private void incrementInflight(long count) {
    if (!enabled) {
      return;
    }
    inflight().add(count, allAttributes);
  }

  private void decrementInflight(long count) {
    if (!enabled) {
      return;
    }
    inflight().add(-count, allAttributes);
  }

  private void incrementExported(long count, @Nullable String errorType) {
    if (!enabled) {
      return;
    }
    Attributes attributes = allAttributes;
    if (errorType != null && !errorType.isEmpty()) {
      attributes = allAttributes.toBuilder().put(ERROR_TYPE_ATTRIB, errorType).build();
    }
    exported().add(count, allAttributes);
  }

  public class Recording {
    /** The number items (spans, log records or metric data points) being exported */
    private final int itemCount;

    private boolean alreadyEnded = false;

    private Recording(int itemCount) {
      this.itemCount = itemCount;
      incrementInflight(itemCount);
    }

    public void finishSuccessful() {
      finish(0, null);
    }

    public void finishPartialSuccess(int rejectedCount) {
      finish(rejectedCount, "rejected");
    }

    public void finishFailed(String errorReason) {
      finish(itemCount, errorReason);
    }

    private void finish(int failedCount, @Nullable String errorType) {
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
    }
  }
}
