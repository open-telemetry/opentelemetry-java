/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.otlp.internal.TlsUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/** Builder utility for {@link OtlpHttpSpanExporter}. */
public final class OtlpHttpSpanExporterBuilder {

  private static final String GRPC_ENDPOINT_PATH =
      "/opentelemetry.proto.collector.trace.v1.TraceService/Export";

  private static final long DEFAULT_TIMEOUT_SECS = 10;
  private static final String DEFAULT_ENDPOINT = "http://localhost:4317/v1/traces";

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private String endpoint = DEFAULT_ENDPOINT;
  private boolean compressionEnabled = false;
  private final Headers.Builder headersBuilder = new Headers.Builder();
  @Nullable private byte[] trustedCertificatesPem;

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpSpanExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpSpanExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT}. The
   * endpoint must start with either http:// or https://, and include the full HTTP path.
   */
  public OtlpHttpSpanExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");

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

    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently the
   * only supported compression method is "gzip".
   */
  public OtlpHttpSpanExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip"),
        "Unsupported compression method. Supported compression methods include: gzip.");
    this.compressionEnabled = true;
    return this;
  }

  /** Add header to requests. */
  public OtlpHttpSpanExporterBuilder addHeader(String key, String value) {
    headersBuilder.add(key, value);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpHttpSpanExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpHttpSpanExporter build() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder().callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        X509TrustManager trustManager = TlsUtil.trustManager(trustedCertificatesPem);
        clientBuilder.sslSocketFactory(TlsUtil.sslSocketFactory(trustManager), trustManager);
      } catch (SSLException e) {
        throw new IllegalStateException(
            "Could not set trusted certificate for OTLP HTTP connection, are they valid X.509 in PEM format?",
            e);
      }
    }

    String endpoint = this.endpoint;

    boolean useGrpc = endpoint.endsWith(GRPC_ENDPOINT_PATH);
    if (useGrpc) {
      if (endpoint.startsWith("http://")) {
        clientBuilder.protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE));
      } else {
        clientBuilder.protocols(Collections.singletonList(Protocol.HTTP_2));
      }

      headersBuilder.add("te", "trailers");
      if (compressionEnabled) {
        headersBuilder.add("grpc-encoding", "gzip");
      }
    } else if (compressionEnabled) {
      headersBuilder.add("Content-Encoding", "gzip");
    }

    Headers headers = headersBuilder.build();

    return new OtlpHttpSpanExporter(
        clientBuilder.build(), endpoint, headers, compressionEnabled, useGrpc);
  }

  OtlpHttpSpanExporterBuilder() {}
}
