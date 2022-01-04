/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.TlsUtil;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.otlp.internal.grpc.OkHttpGrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.okhttp.OkHttpUtil;
import io.opentelemetry.exporter.otlp.internal.okhttp.RetryInterceptor;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

class OkHttpGrpcServiceBuilder<ReqT extends Marshaler, ResT extends UnMarshaller>
    implements GrpcServiceBuilder<ReqT, ResT> {

  private final String type;
  private final String grpcEndpointPath;

  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  private final Headers.Builder headers = new Headers.Builder();
  @Nullable private byte[] trustedCertificatesPem;
  @Nullable private RetryPolicy retryPolicy;
  private MeterProvider meterProvider = MeterProvider.noop();

  /** Creates a new {@link OkHttpGrpcExporterBuilder}. */
  // Visible for testing
  public OkHttpGrpcServiceBuilder(
      String type, String grpcEndpointPath, long defaultTimeoutSecs, URI defaultEndpoint) {
    this.type = type;
    this.grpcEndpointPath = grpcEndpointPath;
    timeoutNanos = TimeUnit.SECONDS.toNanos(defaultTimeoutSecs);
    endpoint = defaultEndpoint;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setChannel(ManagedChannel channel) {
    throw new UnsupportedOperationException("Only available on DefaultGrpcExporter");
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setEndpoint(String endpoint) {
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
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setCompression(String compressionMethod) {
    this.compressionEnabled = true;
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> addHeader(String key, String value) {
    headers.add(key, value);
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> addRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqT, ResT> setMeterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  @Override
  public GrpcService<ReqT, ResT> build() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().dispatcher(OkHttpUtil.newDispatcher());

    Headers.Builder headers = this.headers != null ? this.headers : new Headers.Builder();

    clientBuilder.callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        X509TrustManager trustManager = TlsUtil.trustManager(trustedCertificatesPem);
        clientBuilder.sslSocketFactory(TlsUtil.sslSocketFactory(trustManager), trustManager);
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

    headers.add("te", "trailers");
    if (compressionEnabled) {
      headers.add("grpc-encoding", "gzip");
    }

    if (retryPolicy != null) {
      clientBuilder.addInterceptor(
          new RetryInterceptor(retryPolicy, OkHttpGrpcExporter::isRetryable));
    }

    return new OkHttpGrpcService<>(
        type, clientBuilder.build(), meterProvider, endpoint, headers.build(), compressionEnabled);
  }
}
