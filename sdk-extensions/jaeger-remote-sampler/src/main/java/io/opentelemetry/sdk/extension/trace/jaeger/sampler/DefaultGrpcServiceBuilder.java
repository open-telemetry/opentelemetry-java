/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil.toServiceConfig;

import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.retry.RetryPolicy;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

final class DefaultGrpcServiceBuilder<ReqT extends Marshaler, ResT extends UnMarshaller>
    implements GrpcServiceBuilder<ReqT, ResT> {

  private final String type;
  private final Function<ManagedChannel, MarshalerServiceStub<ReqT, ResT, ?>> stubFactory;
  private final String grpcServiceName;

  @Nullable private ManagedChannel channel;
  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  @Nullable private Metadata metadata;
  @Nullable private byte[] trustedCertificatesPem;
  @Nullable private RetryPolicy retryPolicy;

  /** Creates a new {@link OkHttpGrpcExporterBuilder}. */
  // Visible for testing
  DefaultGrpcServiceBuilder(
      String type,
      Function<ManagedChannel, MarshalerServiceStub<ReqT, ResT, ?>> stubFactory,
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
  public DefaultGrpcServiceBuilder<ReqT, ResT> setChannel(ManagedChannel channel) {
    this.channel = channel;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setEndpoint(String endpoint) {
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
  public DefaultGrpcServiceBuilder<ReqT, ResT> setCompression(String compressionMethod) {
    this.compressionEnabled = true;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTrustedCertificates(
      byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> addHeader(String key, String value) {
    if (metadata == null) {
      metadata = new Metadata();
    }
    metadata.put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> addRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setMeterProvider(MeterProvider meterProvider) {
    return this;
  }

  @Override
  public GrpcService<ReqT, ResT> build() {
    ManagedChannel channel = this.channel;
    if (channel == null) {
      ManagedChannelBuilder<?> managedChannelBuilder =
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
    MarshalerServiceStub<ReqT, ResT, ?> stub =
        stubFactory.apply(channel).withCompression(codec.getMessageEncoding());
    return new DefaultGrpcService<>(type, channel, stub, timeoutNanos);
  }
}
