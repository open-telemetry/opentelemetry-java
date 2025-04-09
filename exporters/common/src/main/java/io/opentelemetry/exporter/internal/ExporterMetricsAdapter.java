package io.opentelemetry.exporter.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Adapter class delegating to {@link ExporterMetrics} and {@link LegacyExporterMetrics} depending on the {@link io.opentelemetry.sdk.common.HealthMetricLevel} setting.
 * This class mimics the interface of {@link ExporterMetrics} to allow it to be easily removed later.
 */
public class ExporterMetricsAdapter {

  @Nullable
  private final ExporterMetrics exporterMetrics;
  @Nullable
  private final LegacyExporterMetrics legacyExporterMetrics;

  public ExporterMetricsAdapter(
      HealthMetricLevel level,
      Supplier<MeterProvider> meterProviderSupplier,
      ExporterMetrics.Signal signal,
      ComponentId componentId,
      @Nullable Attributes additionalAttributes,
      String legacyExporterName,
      String legacyTransportName
  ) {

    if (level == HealthMetricLevel.LEGACY) {
      exporterMetrics = null;
      legacyExporterMetrics = new LegacyExporterMetrics(meterProviderSupplier, legacyExporterName,
          getLegacyType(signal), legacyTransportName);
    } else {
      exporterMetrics = new ExporterMetrics(level, meterProviderSupplier, signal, componentId,
          additionalAttributes);
      legacyExporterMetrics = null;
    }

  }

  private static String getLegacyType(ExporterMetrics.Signal signal) {
    switch (signal) {
      case SPAN:
        return "span";
      case LOG:
        return "log";
      case METRIC:
        return "metric";
    }
    throw new IllegalArgumentException("Unhandled case: " + signal);
  }

  public Recording startRecordingExport(int itemCount) {
    return new Recording(itemCount);
  }


  public class Recording {
    /**
     * The number items (spans, log records or metric data points) being exported
     */
    private final int itemCount;

    @Nullable
    private final ExporterMetrics.Recording delegate;

    private Recording(int itemCount) {
      this.itemCount = itemCount;
      if (legacyExporterMetrics != null) {
        legacyExporterMetrics.addSeen(itemCount);
      } else {
        delegate = exporterMetrics.startRecordingExport(itemCount);
      }
    }

    public void finishSuccessful() {
      if (legacyExporterMetrics != null) {
        finishLegacy(0);
      } else {
        delegate.finishSuccessful();
      }
    }

    public void finishPartialSuccess(int rejectedCount) {
      if (legacyExporterMetrics != null) {
        finishLegacy(rejectedCount);
      } else {
        delegate.finishPartialSuccess(rejectedCount);
      }
    }

    public void finishFailed(Throwable e) {
      //TODO: check in java instrumentation if this is correct
      finishFailed(e.getClass().getName());
    }

    public void finishFailed(String errorReason) {
      if (legacyExporterMetrics != null) {
        finishLegacy(itemCount);
      } else {
        delegate.finishFailed(errorReason);
      }
    }

    private void finishLegacy(int failedCount) {
      int successCount = itemCount - failedCount;
      if (successCount > 0) {
        legacyExporterMetrics.addSuccess(successCount);
      }
      if (failedCount > 0) {
        legacyExporterMetrics.addFailed(failedCount);
      }
    }

  }


}
