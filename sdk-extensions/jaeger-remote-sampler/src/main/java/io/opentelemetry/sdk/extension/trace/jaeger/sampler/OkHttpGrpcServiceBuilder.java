/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.exporter.internal.grpc.OkHttpGrpcExporter;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.okhttp.OkHttpUtil;
import io.opentelemetry.exporter.internal.retry.RetryInterceptor;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

final class OkHttpGrpcServiceBuilder<
        ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler>
    implements GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> {

  private final String type;
  private final String grpcEndpointPath;

  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  private final Headers.Builder headers = new Headers.Builder();
  @Nullable private byte[] trustedCertificatesPem;
  @Nullable private byte[][] clientKeysPem;
  @Nullable private RetryPolicy retryPolicy;

  OkHttpGrpcServiceBuilder(
      String type, String grpcEndpointPath, long defaultTimeoutSecs, URI defaultEndpoint) {
    this.type = type;
    this.grpcEndpointPath = grpcEndpointPath;
    timeoutNanos = TimeUnit.SECONDS.toNanos(defaultTimeoutSecs);
    endpoint = defaultEndpoint;
  }

  @Override
  public GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setChannel(ManagedChannel channel) {
    throw new UnsupportedOperationException("Only available on DefaultGrpcService");
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTimeout(
      long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    checkArgument(!timeout.isNegative(), "timeout must be non-negative");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setCompression(
      String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    this.compressionEnabled = true;
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTrustedCertificates(
      byte[] trustedCertificatesPem) {
    requireNonNull(trustedCertificatesPem, "trustedCertificatesPem");
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  @Override
  public GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setClientKeys(byte[][] clientKeysPem) {
    requireNonNull(clientKeysPem, "clientKeysPem");
    this.clientKeysPem = clientKeysPem;
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> addHeader(
      String key, String value) {
    requireNonNull(key, "key");
    requireNonNull(value, "value");
    headers.add(key, value);
    return this;
  }

  @Override
  public OkHttpGrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> addRetryPolicy(
      RetryPolicy retryPolicy) {
    requireNonNull(retryPolicy, "retryPolicy");
    this.retryPolicy = retryPolicy;
    return this;
  }

  @Override
  public GrpcService<ReqMarshalerT, ResUnMarshalerT> build() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().dispatcher(OkHttpUtil.newDispatcher());

    clientBuilder.callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        X509TrustManager trustManager = TlsUtil.trustManager(trustedCertificatesPem);
        X509KeyManager keyManager = null;
        if (clientKeysPem!=null) {
        	keyManager = TlsUtil.keyManager(clientKeysPem);
        }
        clientBuilder.sslSocketFactory(TlsUtil.sslSocketFactory(keyManager, trustManager), trustManager);
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
        type, clientBuilder.build(), endpoint, headers.build(), compressionEnabled);
  }
}
