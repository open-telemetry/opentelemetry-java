/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetrySchemaVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ExporterInstrumentation implements ExporterMetrics {

  private final ExporterMetrics implementation;

  public ExporterInstrumentation(
      InternalTelemetrySchemaVersion schema,
      Supplier<MeterProvider> meterProviderSupplier,
      ComponentId componentId,
      ComponentId.StandardExporterType exporterType,
      @Nullable Attributes additionalAttributes) {

    switch (schema) {
      case DISABLED:
        implementation = NoopExporterMetrics.INSTANCE;
        break;
      case LEGACY:
        implementation =
            LegacyExporterMetrics.isSupportedType(exporterType)
                ? new LegacyExporterMetrics(meterProviderSupplier, exporterType)
                : NoopExporterMetrics.INSTANCE;
        break;
      case V1_33:
      case LATEST:
        implementation =
            new SemConvExporterMetrics(
                meterProviderSupplier,
                exporterType.signal(),
                componentId,
                additionalAttributes == null ? Attributes.empty() : additionalAttributes);
        break;
      default:
        throw new IllegalStateException("Unhandled case: " + schema);
    }
  }

  @Override
  public Recording startRecordingExport(int itemCount) {
    return new Recording(implementation.startRecordingExport(itemCount));
  }

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public static class Recording extends ExporterMetrics.Recording {

    private final ExporterMetrics.Recording delegate;

    private Recording(ExporterMetrics.Recording delegate) {
      this.delegate = delegate;
    }

    /**
     * Callback to notify that the export has failed with the given {@link Throwable} as failure
     * cause.
     *
     * @param failureCause the cause of the failure
     * @param requestAttributes additional attributes to add to request metrics
     */
    public final void finishFailed(Throwable failureCause, Attributes requestAttributes) {
      finishFailed(failureCause.getClass().getName(), requestAttributes);
    }

    @Override
    protected void doFinish(@Nullable String errorType, Attributes requestAttributes) {
      delegate.doFinish(errorType, requestAttributes);
    }
  }
}
