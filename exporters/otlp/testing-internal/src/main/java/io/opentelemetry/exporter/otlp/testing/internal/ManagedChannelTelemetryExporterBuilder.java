/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;

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

  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();

  @Override
  public TelemetryExporterBuilder<T> setEndpoint(String endpoint) {
    delegate.setEndpoint(endpoint);
    URI uri = URI.create(endpoint);
    channelBuilder = ManagedChannelBuilder.forAddress(uri.getHost(), uri.getPort());
    if (!uri.getScheme().equals("https")) {
      channelBuilder.usePlaintext();
    }
    // User-Agent can only be set at the channel level with upstream gRPC client. If a user wants
    // the User-Agent to be spec compliant they must manually set the user agent when building
    // their channel.
    channelBuilder.userAgent(OtlpUserAgent.getUserAgent());
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
  public TelemetryExporterBuilder<T> setConnectTimeout(long timeout, TimeUnit unit) {
    delegate.setConnectTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setConnectTimeout(Duration timeout) {
    delegate.setConnectTimeout(timeout);
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

  @Override
  public TelemetryExporterBuilder<T> setHeaders(Supplier<Map<String, String>> headerSupplier) {
    delegate.setHeaders(headerSupplier);
    return this;
  }

  // When a user provides a Channel, we are not in control of TLS or retry config and reimplement it
  // here for use in tests. Technically we don't have to test them since they are out of the SDK's
  // control, but it's probably worth verifying the baseline functionality anyways.

  @Override
  public TelemetryExporterBuilder<T> setTrustedCertificates(byte[] certificates) {
    delegate.setTrustedCertificates(certificates);
    tlsConfigHelper.setTrustManagerFromCerts(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    delegate.setClientTls(privateKeyPem, certificatePem);
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    delegate.setRetryPolicy(retryPolicy);
    if (retryPolicy == null) {
      return this;
    }
    String grpcServiceName;
    if (delegate instanceof GrpcLogRecordExporterBuilderWrapper) {
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
  public TelemetryExporterBuilder<T> setProxyOptions(ProxyOptions proxyOptions) {
    delegate.setProxyOptions(proxyOptions);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setChannel(Object channel) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TelemetryExporterBuilder<T> setServiceClassLoader(ClassLoader serviceClassLoader) {
    delegate.setServiceClassLoader(serviceClassLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setComponentLoader(ComponentLoader componentLoader) {
    delegate.setComponentLoader(componentLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setExecutorService(ExecutorService executorService) {
    delegate.setExecutorService(executorService);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    delegate.setMeterProvider(meterProviderSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<T> setInternalTelemetryVersion(
      InternalTelemetryVersion schemaVersion) {
    delegate.setInternalTelemetryVersion(schemaVersion);
    return this;
  }

  @Override
  public TelemetryExporter<T> build() {
    Runnable shutdownCallback;
    if (channelBuilder != null) {
      try {
        setSslContext(channelBuilder, tlsConfigHelper);
      } catch (SSLException e) {
        throw new IllegalStateException(e);
      }

      ManagedChannel channel = channelBuilder.build();
      delegate.setChannel(channel);
      shutdownCallback = channel::shutdownNow;
    } else {
      shutdownCallback = () -> {};
    }

    TelemetryExporter<T> delegateExporter = delegate.build();
    return new TelemetryExporter<T>() {
      @Override
      public Object unwrap() {
        return delegateExporter.unwrap();
      }

      @Override
      public CompletableResultCode export(Collection<T> items) {
        return delegateExporter.export(items);
      }

      @Override
      public CompletableResultCode shutdown() {
        shutdownCallback.run();
        return delegateExporter.shutdown();
      }
    };
  }

  @Override
  public TelemetryExporterBuilder<T> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    delegate.setSslContext(sslContext, trustManager);
    tlsConfigHelper.setSslContext(sslContext, trustManager);
    return this;
  }

  /**
   * Configure the channel builder to trust the certificates. The {@code byte[]} should contain an
   * X.509 certificate collection in PEM format.
   *
   * @throws SSLException if error occur processing the certificates
   */
  private static void setSslContext(
      ManagedChannelBuilder<?> managedChannelBuilder, TlsConfigHelper tlsConfigHelper)
      throws SSLException {
    X509TrustManager trustManager = tlsConfigHelper.getTrustManager();
    if (trustManager == null) {
      return;
    }

    // gRPC does not abstract TLS configuration so we need to check the implementation and act
    // accordingly.
    String channelBuilderClassName = managedChannelBuilder.getClass().getName();
    switch (channelBuilderClassName) {
      case "io.grpc.netty.NettyChannelBuilder":
        {
          NettyChannelBuilder nettyBuilder = (NettyChannelBuilder) managedChannelBuilder;
          SslContext sslContext =
              GrpcSslContexts.forClient()
                  .keyManager(tlsConfigHelper.getKeyManager())
                  .trustManager(trustManager)
                  .build();
          nettyBuilder.sslContext(sslContext);
          break;
        }
      case "io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder":
        {
          io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder nettyBuilder =
              (io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder) managedChannelBuilder;
          io.grpc.netty.shaded.io.netty.handler.ssl.SslContext sslContext =
              io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forClient()
                  .trustManager(trustManager)
                  .keyManager(tlsConfigHelper.getKeyManager())
                  .build();
          nettyBuilder.sslContext(sslContext);
          break;
        }
      case "io.grpc.okhttp.OkHttpChannelBuilder":
        SSLContext sslContext = tlsConfigHelper.getSslContext();
        if (sslContext == null) {
          return;
        }
        io.grpc.okhttp.OkHttpChannelBuilder okHttpBuilder =
            (io.grpc.okhttp.OkHttpChannelBuilder) managedChannelBuilder;
        okHttpBuilder.sslSocketFactory(sslContext.getSocketFactory());
        break;
      default:
        throw new SSLException(
            "TLS certificate configuration not supported for unrecognized ManagedChannelBuilder "
                + channelBuilderClassName);
    }
  }
}
