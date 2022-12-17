/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.okhttp.OkHttpUtil;
import io.opentelemetry.exporter.internal.retry.RetryInterceptor;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * A builder for {@link GrpcExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("JavadocMethod")
public class GrpcExporterBuilder<T extends Marshaler> {

  private final String exporterName;
  private final String type;
  private final String grpcEndpointPath;
  private final Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>>
      grpcStubFactory;

  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  private final Map<String, String> headers = new HashMap<>();
  @Nullable private byte[] trustedCertificatesPem;
  @Nullable private byte[] privateKeyPem;
  @Nullable private byte[] certificatePem;
  @Nullable private RetryPolicy retryPolicy;
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;

  // Use Object type since gRPC may not be on the classpath.
  @Nullable private Object grpcChannel;

  GrpcExporterBuilder(
      String exporterName,
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> grpcStubFactory,
      String grpcEndpointPath) {
    this.exporterName = exporterName;
    this.type = type;
    this.grpcEndpointPath = grpcEndpointPath;
    timeoutNanos = TimeUnit.SECONDS.toNanos(defaultTimeoutSecs);
    endpoint = defaultEndpoint;
    this.grpcStubFactory = grpcStubFactory;
  }

  public GrpcExporterBuilder<T> setChannel(ManagedChannel channel) {
    this.grpcChannel = channel;
    return this;
  }

  public GrpcExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  public GrpcExporterBuilder<T> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  public GrpcExporterBuilder<T> setEndpoint(String endpoint) {
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  public GrpcExporterBuilder<T> setCompression(String compressionMethod) {
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  public GrpcExporterBuilder<T> setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  public GrpcExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    this.privateKeyPem = privateKeyPem;
    this.certificatePem = certificatePem;
    return this;
  }

  public GrpcExporterBuilder<T> addHeader(String key, String value) {
    headers.put(key, value);
    return this;
  }

  public GrpcExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public GrpcExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProviderSupplier = () -> meterProvider;
    return this;
  }

  public GrpcExporter<T> build() {
    if (grpcChannel != null) {
      return new UpstreamGrpcExporterFactory().buildWithChannel((Channel) grpcChannel);
    }

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().dispatcher(OkHttpUtil.newDispatcher());

    clientBuilder.callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        X509TrustManager trustManager = TlsUtil.trustManager(trustedCertificatesPem);
        X509KeyManager keyManager = null;
        if (privateKeyPem != null && certificatePem != null) {
          keyManager = TlsUtil.keyManager(privateKeyPem, certificatePem);
        }
        clientBuilder.sslSocketFactory(
            TlsUtil.sslSocketFactory(keyManager, trustManager), trustManager);
      } catch (SSLException e) {
        throw new IllegalStateException(
            "Could not set trusted certificates, are they valid X.509 in PEM format?", e);
      }
    }

    String endpoint = this.endpoint.resolve(grpcEndpointPath).toString();
    if (endpoint.startsWith("http://")) {
      clientBuilder.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE));
    } else {
      clientBuilder.protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1));
    }

    Headers.Builder headers = new Headers.Builder();
    this.headers.forEach(headers::add);

    headers.add("te", "trailers");
    if (compressionEnabled) {
      headers.add("grpc-encoding", "gzip");
    }

    if (retryPolicy != null) {
      clientBuilder.addInterceptor(
          new RetryInterceptor(retryPolicy, OkHttpGrpcExporter::isRetryable));
    }

    return new OkHttpGrpcExporter<>(
        exporterName,
        type,
        clientBuilder.build(),
        meterProviderSupplier,
        endpoint,
        headers.build(),
        compressionEnabled);
  }

  // Use an inner class to ensure GrpcExporterBuilder does not have classloading dependencies on
  // upstream gRPC.
  private class UpstreamGrpcExporterFactory {
    private GrpcExporter<T> buildWithChannel(Channel channel) {
      Metadata metadata = new Metadata();
      String authorityOverride = null;
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        String name = entry.getKey();
        String value = entry.getValue();
        if (name.equals("host")) {
          authorityOverride = value;
          continue;
        }
        metadata.put(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER), value);
      }

      channel =
          ClientInterceptors.intercept(
              channel, MetadataUtils.newAttachHeadersInterceptor(metadata));

      Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
      MarshalerServiceStub<T, ?, ?> stub =
          grpcStubFactory
              .get()
              .apply(channel, authorityOverride)
              .withCompression(codec.getMessageEncoding());
      return new UpstreamGrpcExporter<>(
          exporterName, type, stub, meterProviderSupplier, timeoutNanos);
    }
  }
}
