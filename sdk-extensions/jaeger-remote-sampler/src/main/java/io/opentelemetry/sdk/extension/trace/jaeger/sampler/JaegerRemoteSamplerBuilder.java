/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.sender.okhttp.internal.OkHttpUtil;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/** A builder for {@link JaegerRemoteSampler}. */
public final class JaegerRemoteSamplerBuilder {

  private static final String GRPC_SERVICE_NAME = "jaeger.api_v2.SamplingManager";
  // Visible for testing
  static final String GRPC_ENDPOINT_PATH = "/" + GRPC_SERVICE_NAME + "/GetSamplingStrategy";

  private static final String DEFAULT_ENDPOINT_URL = "http://localhost:14250";
  private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
  private static final int DEFAULT_POLLING_INTERVAL_MILLIS = 60000;
  private static final Sampler INITIAL_SAMPLER =
      Sampler.parentBased(Sampler.traceIdRatioBased(0.001));
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  private URI endpoint = DEFAULT_ENDPOINT;
  private Sampler initialSampler = INITIAL_SAMPLER;
  private int pollingIntervalMillis = DEFAULT_POLLING_INTERVAL_MILLIS;
  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();

  @Nullable private String serviceName;

  // Use Object type since gRPC may not be on the classpath.
  @Nullable private Object grpcChannel;

  JaegerRemoteSamplerBuilder() {}

  /**
   * Sets the service name to be used by this exporter. Required.
   *
   * @param serviceName the service name.
   * @return this.
   */
  public JaegerRemoteSamplerBuilder setServiceName(String serviceName) {
    requireNonNull(serviceName, "serviceName");
    this.serviceName = serviceName;
    return this;
  }

  /**
   * Sets the Jaeger endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT_URL}.
   */
  public JaegerRemoteSamplerBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  /** Sets trusted certificate. */
  public JaegerRemoteSamplerBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    requireNonNull(trustedCertificatesPem, "trustedCertificatesPem");
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  /**
   * Sets the client key and the certificate chain to use for verifying client when TLS is enabled.
   * The key must be PKCS8, and both must be in PEM format.
   *
   * @since 1.24.0
   */
  public JaegerRemoteSamplerBuilder setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    requireNonNull(privateKeyPem, "privateKeyPem");
    requireNonNull(certificatePem, "certificatePem");
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  /**
   * Sets the "bring-your-own" SSLContext for use with TLS. Users should call this _or_ set raw
   * certificate bytes, but not both.
   */
  public JaegerRemoteSamplerBuilder setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
    return this;
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_MILLIS}ms. Must be positive.
   */
  public JaegerRemoteSamplerBuilder setPollingInterval(int interval, TimeUnit unit) {
    requireNonNull(unit, "unit");
    Utils.checkArgument(interval > 0, "polling interval must be positive");
    pollingIntervalMillis = (int) unit.toMillis(interval);
    return this;
  }

  /**
   * Sets the polling interval for configuration updates. If unset, defaults to {@value
   * DEFAULT_POLLING_INTERVAL_MILLIS}ms.
   */
  public JaegerRemoteSamplerBuilder setPollingInterval(Duration interval) {
    requireNonNull(interval, "interval");
    return setPollingInterval((int) interval.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Sets the initial sampler that is used before sampling configuration is obtained. If unset,
   * defaults to a parent-based ratio-based sampler with a ratio of 0.001.
   */
  public JaegerRemoteSamplerBuilder setInitialSampler(Sampler initialSampler) {
    requireNonNull(initialSampler, "initialSampler");
    this.initialSampler = initialSampler;
    return this;
  }

  /**
   * Sets the managed channel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   *
   * @deprecated Use {@link #setEndpoint(String)}. If you have a use case not satisfied by the
   *     methods on this builder, please file an issue to let us know what it is.
   */
  @Deprecated
  public JaegerRemoteSamplerBuilder setChannel(ManagedChannel channel) {
    requireNonNull(channel, "channel");
    this.grpcChannel = channel;
    return this;
  }

  /**
   * Builds the {@link JaegerRemoteSampler}.
   *
   * @return the remote sampler instance.
   */
  public JaegerRemoteSampler build() {
    if (grpcChannel != null) {
      return new JaegerRemoteSampler(
          UpstreamGrpcExporterFactory.buildWithChannel((ManagedChannel) grpcChannel),
          serviceName,
          pollingIntervalMillis,
          initialSampler);
    }

    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().dispatcher(OkHttpUtil.newDispatcher());

    clientBuilder.callTimeout(Duration.ofNanos(TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS)));

    String endpoint = this.endpoint.resolve(GRPC_ENDPOINT_PATH).toString();
    boolean isPlainHttp = endpoint.startsWith("http://");

    SSLContext sslContext = isPlainHttp ? null : tlsConfigHelper.getSslContext();
    X509TrustManager trustManager = isPlainHttp ? null : tlsConfigHelper.getTrustManager();
    if (sslContext != null && trustManager != null) {
      clientBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }

    if (isPlainHttp) {
      clientBuilder.connectionSpecs(Collections.singletonList(ConnectionSpec.CLEARTEXT));
      clientBuilder.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE));
    } else {
      clientBuilder.protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1));
    }

    Headers.Builder headers = new Headers.Builder();
    headers.add("te", "trailers");

    return new JaegerRemoteSampler(
        new OkHttpGrpcService("remoteSampling", clientBuilder.build(), endpoint, headers.build()),
        serviceName,
        pollingIntervalMillis,
        initialSampler);
  }

  // Use an inner class to ensure GrpcExporterBuilder does not have classloading dependencies on
  // upstream gRPC.
  private static class UpstreamGrpcExporterFactory {
    private static GrpcService buildWithChannel(ManagedChannel channel) {
      return new UpstreamGrpcService(
          "remoteSampling",
          channel,
          MarshallerRemoteSamplerServiceGrpc.getPostSpansMethod,
          TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS));
    }
  }
}
