/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

/**
 * Wraps a {@link TelemetryExporterBuilder}, delegating methods to upstream gRPC's {@link
 * ManagedChannel} where appropriate.
 */
public final class ManagedChannelTelemetryExporterBuilder<T>
    implements TelemetryExporterBuilder<T> {

  public static <T> ManagedChannelTelemetryExporterBuilder<T> wrap(
      TelemetryExporterBuilder<T> delegate) {
    return new ManagedChannelTelemetryExporterBuilder<>(delegate);
  }

  private ManagedChannelTelemetryExporterBuilder(TelemetryExporterBuilder<T> delegate) {
    this.delegate = delegate;
  }

  private final TelemetryExporterBuilder<T> delegate;

  @Nullable private ManagedChannelBuilder<?> channelBuilder;

  @Nullable private byte[] privateKeyPem;
  @Nullable private byte[] certificatePem;
  @Nullable private byte[] trustedCertificatesPem;

  @Override
  public TelemetryExporterBuilder<T> setEndpoint(String endpoint) {
    delegate.setEndpoint(endpoint);
    URI uri = URI.create(endpoint);
    channelBuilder = ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort());
    if (!uri.getScheme().equals("https")) {
      channelBuilder.usePlaintext();
    }
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    delegate.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setTimeout(Duration timeout) {
    delegate.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setCompression(String compression) {
    delegate.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> addHeader(String key, String value) {
    delegate.addHeader(key, value);
    return this;
  }

  // When a user provides a Channel, we are not in control of TLS or retry config and reimplement it
  // here for use in tests. Technically we don't have to test them since they are out of the SDK's
  // control, but it's probably worth verifying the baseline functionality anyways.

  @Override
  public TelemetryExporterBuilder<T> setTrustedCertificates(byte[] certificates) {
    this.trustedCertificatesPem = certificates;
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    this.privateKeyPem = privateKeyPem;
    this.certificatePem = certificatePem;
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy) {
    String grpcServiceName;
    if (delegate instanceof GrpcLogExporterBuilderWrapper) {
      grpcServiceName = "opentelemetry.proto.collector.logs.v1.LogsService";
    } else if (delegate instanceof GrpcMetricExporterBuilderWrapper) {
      grpcServiceName = "opentelemetry.proto.collector.metrics.v1.MetricsService";
    } else if (delegate instanceof GrpcSpanExporterBuilderWrapper) {
      grpcServiceName = "opentelemetry.proto.collector.trace.v1.TraceService";
    } else {
      throw new IllegalStateException("Can't happen");
    }
    requireNonNull(channelBuilder, "channel");
    channelBuilder.defaultServiceConfig(
        ManagedChannelUtil.toServiceConfig(grpcServiceName, retryPolicy));
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setChannel(ManagedChannel channel) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TelemetryExporter<T> build() {
    requireNonNull(channelBuilder, "channel");
    if (trustedCertificatesPem != null) {
      try {
        ManagedChannelUtil.setClientKeysAndTrustedCertificatesPem(
            channelBuilder, privateKeyPem, certificatePem, trustedCertificatesPem);
      } catch (SSLException e) {
        throw new IllegalStateException(
            "Could not set trusted certificates, are they valid X.509 in PEM format?", e);
      }
    }
    ManagedChannel channel = channelBuilder.build();
    delegate.setChannel(channel);
    TelemetryExporter<T> delegateExporter = delegate.build();
    return new TelemetryExporter<T>() {
      @Override
      public CompletableResultCode export(Collection<T> items) {
        return delegateExporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        channel.shutdownNow();
        return delegateExporter.shutdown();
      }
    };
  }
}
