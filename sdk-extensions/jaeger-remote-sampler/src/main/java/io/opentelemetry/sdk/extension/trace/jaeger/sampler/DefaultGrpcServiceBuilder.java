/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil.toServiceConfig;
import static java.util.Objects.requireNonNull;

import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;

final class DefaultGrpcServiceBuilder<ReqT extends Marshaler, ResT extends UnMarshaler>
    implements GrpcServiceBuilder<ReqT, ResT> {

  private final String type;
  private final Function<ManagedChannel, MarshalerServiceStub<ReqT, ResT, ?>> stubFactory;
  private final String grpcServiceName;

  @Nullable private ManagedChannel channel;
  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  @Nullable private Metadata metadata;
  @Nullable private RetryPolicy retryPolicy;
  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();

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
    requireNonNull(channel, "channel");
    this.channel = channel;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    checkArgument(!timeout.isNegative(), "timeout must be non-negative");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    this.compressionEnabled = true;
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> setTrustedCertificates(
      byte[] trustedCertificatesPem) {
    requireNonNull(trustedCertificatesPem, "trustedCertificatesPem");
    tlsConfigHelper.createTrustManager(trustedCertificatesPem);
    return this;
  }

  @Override
  public GrpcServiceBuilder<ReqT, ResT> setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.createKeyManager(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> addHeader(String key, String value) {
    requireNonNull(key, "key");
    requireNonNull(value, "value");
    if (metadata == null) {
      metadata = new Metadata();
    }
    metadata.put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value);
    return this;
  }

  @Override
  public DefaultGrpcServiceBuilder<ReqT, ResT> addRetryPolicy(RetryPolicy retryPolicy) {
    requireNonNull(retryPolicy, "retryPolicy");
    this.retryPolicy = retryPolicy;
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

      tlsConfigHelper.configureWithKeyManager(
          (tm, km) ->
              ManagedChannelUtil.setClientKeysAndTrustedCertificatesPem(
                  managedChannelBuilder, tm, km));

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
