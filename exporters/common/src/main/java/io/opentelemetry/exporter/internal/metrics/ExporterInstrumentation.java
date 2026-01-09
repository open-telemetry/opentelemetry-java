/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.internal.Signal;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ExporterInstrumentation {

  private final ExporterMetrics implementation;

  public ExporterInstrumentation(
      InternalTelemetryVersion schema,
      Supplier<MeterProvider> meterProviderSupplier,
      StandardComponentId componentId,
      String endpoint) {

    Signal signal = componentId.getStandardType().signal();
    switch (schema) {
      case LEGACY:
        implementation =
            LegacyExporterMetrics.isSupportedType(componentId.getStandardType())
                ? new LegacyExporterMetrics(meterProviderSupplier, componentId.getStandardType())
                : NoopExporterMetrics.INSTANCE;
        break;
      case LATEST:
        implementation =
            signal == Signal.PROFILE
                ? NoopExporterMetrics.INSTANCE
                : new SemConvExporterMetrics(
                    meterProviderSupplier, signal, componentId, extractServerAttributes(endpoint));
        break;
      default:
        throw new IllegalStateException("Unhandled case: " + schema);
    }
  }

  // visible for testing
  static Attributes extractServerAttributes(String httpEndpoint) {
    try {
      URI parsed = new URI(httpEndpoint);
      AttributesBuilder builder = Attributes.builder();
      String host = parsed.getHost();
      if (host != null) {
        builder.put(SemConvAttributes.SERVER_ADDRESS, host);
      }
      int port = parsed.getPort();
      if (port == -1) {
        String scheme = parsed.getScheme();
        if ("https".equals(scheme)) {
          port = 443;
        } else if ("http".equals(scheme)) {
          port = 80;
        }
      }
      if (port != -1) {
        builder.put(SemConvAttributes.SERVER_PORT, port);
      }
      return builder.build();
    } catch (URISyntaxException e) {
      return Attributes.empty();
    }
  }

  public Recording startRecordingExport(int itemCount) {
    return new Recording(implementation.startRecordingExport(itemCount));
  }

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  public static class Recording {

    private final ExporterMetrics.Recording delegate;
    @Nullable private Long httpStatusCode;
    @Nullable private Long grpcStatusCode;

    private Recording(ExporterMetrics.Recording delegate) {
      this.delegate = delegate;
    }

    public void setHttpStatusCode(long httpStatusCode) {
      if (grpcStatusCode != null) {
        throw new IllegalStateException(
            "gRPC status code already set, can only set either gRPC or HTTP");
      }
      this.httpStatusCode = httpStatusCode;
    }

    public void setGrpcStatusCode(long grpcStatusCode) {
      if (httpStatusCode != null) {
        throw new IllegalStateException(
            "HTTP status code already set, can only set either gRPC or HTTP");
      }
      this.grpcStatusCode = grpcStatusCode;
    }

    /** Callback to notify that the export was successful. */
    public void finishSuccessful() {
      delegate.finishSuccessful(buildRequestAttributes());
    }

    /**
     * Callback to notify that the export has failed with the given {@link Throwable} as failure
     * cause.
     *
     * @param failureCause the cause of the failure
     */
    public void finishFailed(Throwable failureCause) {
      finishFailed(failureCause.getClass().getName());
    }

    /**
     * Callback to notify that the export has failed.
     *
     * @param errorType a failure reason suitable for the error.type attribute
     */
    public void finishFailed(String errorType) {
      delegate.finishFailed(errorType, buildRequestAttributes());
    }

    private Attributes buildRequestAttributes() {
      if (httpStatusCode != null) {
        return Attributes.of(SemConvAttributes.HTTP_RESPONSE_STATUS_CODE, httpStatusCode);
      }
      if (grpcStatusCode != null) {
        return Attributes.of(SemConvAttributes.RPC_GRPC_STATUS_CODE, grpcStatusCode);
      }
      return Attributes.empty();
    }
  }
}
