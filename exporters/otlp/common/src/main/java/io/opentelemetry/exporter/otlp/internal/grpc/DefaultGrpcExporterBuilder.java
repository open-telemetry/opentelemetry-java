/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil.toServiceConfig;

import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

/**
 * A builder for {@link DefaultGrpcExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultGrpcExporterBuilder<T extends Marshaler>
    implements GrpcExporterBuilder<T> {

  private final String type;
  private final Function<ManagedChannel, MarshalerServiceStub<T, ?, ?>> stubFactory;
  private final String grpcServiceName;

  @Nullable private ManagedChannel channel;
  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  @Nullable private Metadata metadata;
  @Nullable private byte[] trustedCertificatesPem;
  @Nullable private RetryPolicy retryPolicy;
  private MeterProvider meterProvider = MeterProvider.noop();

  /** Creates a new {@link DefaultGrpcExporterBuilder}. */
  // Visible for testing
  public DefaultGrpcExporterBuilder(
      String type,
      Function<ManagedChannel, MarshalerServiceStub<T, ?, ?>> stubFactory,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      String grpcServiceName) {
    this.type = type;
    this.stubFactory = stubFactory;
    this.grpcServiceName = grpcServiceName;
    timeoutNanos = TimeUnit.SECONDS.toNanos(defaultTimeoutSecs);
    endpoint = defaultEndpoint;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setChannel(ManagedChannel channel) {
    this.channel = channel;
    return this;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setEndpoint(String endpoint) {
    URI uri;
    try {
      uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
    }

    if (uri.getScheme() == null
        || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }

    this.endpoint = uri;
    return this;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setCompression(String compressionMethod) {
    this.compressionEnabled = true;
    return this;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  @Override
  public DefaultGrpcExporterBuilder<T> addHeader(String key, String value) {
    if (metadata == null) {
      metadata = new Metadata();
    }
    metadata.put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value);
    return this;
  }

  @Override
  public GrpcExporterBuilder<T> addRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  @Override
  public GrpcExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  @Override
  public GrpcExporter<T> build() {
    ManagedChannel channel = this.channel;
    if (channel == null) {
      final ManagedChannelBuilder<?> managedChannelBuilder =
          ManagedChannelBuilder.forTarget(endpoint.getAuthority());

      if (endpoint.getScheme().equals("https")) {
        managedChannelBuilder.useTransportSecurity();
      } else {
        managedChannelBuilder.usePlaintext();
      }

      if (metadata != null) {
        managedChannelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));
      }

      if (trustedCertificatesPem != null) {
        try {
          ManagedChannelUtil.setTrustedCertificatesPem(
              managedChannelBuilder, trustedCertificatesPem);
        } catch (SSLException e) {
          throw new IllegalStateException(
              "Could not set trusted certificates for gRPC TLS connection, are they valid "
                  + "X.509 in PEM format?",
              e);
        }
      }

      if (retryPolicy != null) {
        managedChannelBuilder.defaultServiceConfig(toServiceConfig(grpcServiceName, retryPolicy));
      }

      channel = managedChannelBuilder.build();
    }

    Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
    MarshalerServiceStub<T, ?, ?> stub =
        stubFactory.apply(channel).withCompression(codec.getMessageEncoding());
    return new DefaultGrpcExporter<>(
        type, channel, stub, meterProvider, timeoutNanos, compressionEnabled);
  }

  /**
   * Reflectively access a {@link DefaultGrpcExporterBuilder} instance in field called "delegate" of
   * the instance.
   *
   * @throws IllegalArgumentException if the instance does not contain a field called "delegate" of
   *     type {@link DefaultGrpcExporterBuilder}
   */
  public static <T> DefaultGrpcExporterBuilder<?> getDelegateBuilder(Class<T> type, T instance) {
    try {
      Field field = type.getDeclaredField("delegate");
      field.setAccessible(true);
      Object value = field.get(instance);
      if (!(value instanceof DefaultGrpcExporterBuilder)) {
        throw new IllegalArgumentException("delegate field is not type DefaultGrpcExporterBuilder");
      }
      return (DefaultGrpcExporterBuilder<?>) value;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalArgumentException("Unable to access delegate reflectively.", e);
    }
  }
}
