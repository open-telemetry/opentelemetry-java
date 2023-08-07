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
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.okhttp.OkHttpUtil;
import io.opentelemetry.exporter.internal.retry.RetryInterceptor;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
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
  private TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
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

  public GrpcExporterBuilder<T> setTrustManagerFromCerts(byte[] trustedCertificatesPem) {
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  public GrpcExporterBuilder<T> setKeyManagerFromCerts(
      byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  public GrpcExporterBuilder<T> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
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

  @SuppressWarnings("BuilderReturnThis")
  public GrpcExporterBuilder<T> copy() {
    GrpcExporterBuilder<T> copy =
        new GrpcExporterBuilder<>(
            exporterName,
            type,
            TimeUnit.NANOSECONDS.toSeconds(timeoutNanos),
            endpoint,
            grpcStubFactory,
            grpcEndpointPath);

    copy.timeoutNanos = timeoutNanos;
    copy.endpoint = endpoint;
    copy.compressionEnabled = compressionEnabled;
    copy.headers.putAll(headers);
    copy.tlsConfigHelper = tlsConfigHelper.copy();
    if (retryPolicy != null) {
      copy.retryPolicy = retryPolicy.toBuilder().build();
    }
    copy.meterProviderSupplier = meterProviderSupplier;
    copy.grpcChannel = grpcChannel;
    return copy;
  }

  public GrpcExporter<T> build() {
    if (grpcChannel != null) {
      return new UpstreamGrpcExporterFactory().buildWithChannel((Channel) grpcChannel);
    }

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().dispatcher(OkHttpUtil.newDispatcher());

    clientBuilder.callTimeout(Duration.ofNanos(timeoutNanos));

    SSLContext sslContext = tlsConfigHelper.getSslContext();
    X509TrustManager trustManager = tlsConfigHelper.getTrustManager();
    if (sslContext != null && trustManager != null) {
      clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
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

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "GrpcExporterBuilder{", "}");
    joiner.add("exporterName=" + exporterName);
    joiner.add("type=" + type);
    joiner.add("endpoint=" + endpoint.toString());
    joiner.add("endpointPath=" + grpcEndpointPath);
    joiner.add("timeoutNanos=" + timeoutNanos);
    joiner.add("compressionEnabled=" + compressionEnabled);
    StringJoiner headersJoiner = new StringJoiner(", ", "Headers{", "}");
    headers.forEach((key, value) -> headersJoiner.add(key + "=OBFUSCATED"));
    joiner.add("headers=" + headersJoiner);
    if (retryPolicy != null) {
      joiner.add("retryPolicy=" + retryPolicy);
    }
    if (grpcChannel != null) {
      joiner.add("grpcChannel=" + grpcChannel);
    }
    // Note: omit tlsConfigHelper because we can't log the configuration in any readable way
    // Note: omit meterProviderSupplier because we can't log the configuration in any readable way
    return joiner.toString();
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
