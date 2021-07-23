/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import io.opentelemetry.exporter.otlp.trace.OtlpHttpSpanExporter.RequestResponseHandler;
import io.opentelemetry.internal.shaded.okhttp3.Dispatcher;
import io.opentelemetry.internal.shaded.okhttp3.Headers;
import io.opentelemetry.internal.shaded.okhttp3.OkHttpClient;
import io.opentelemetry.internal.shaded.okhttp3.tls.HandshakeCertificates;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder utility for this exporter. */
public final class OtlpHttpSpanExporterBuilder {

  private static final long DEFAULT_TIMEOUT_SECS = 10;
  private static final String DEFAULT_ENDPOINT = "http://localhost:4317/v1/traces";
  private static final Encoding DEFAULT_ENCODING = Encoding.PROTOBUF;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private String endpoint = DEFAULT_ENDPOINT;
  private Encoding encoding = DEFAULT_ENCODING;
  private boolean isCompressionEnabled = false;
  @Nullable private Headers.Builder headersBuilder;
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
   * endpoint must start with either http:// or https://, and should append the version and signal
   * to the path (i.e. v1/traces).
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
   * Sets the encoding of payloads to be JSON format. If unset, defaults Protobuf format. NOTE: JSON
   * format is currently experimental.
   */
  public OtlpHttpSpanExporterBuilder setJsonEncoding() {
    this.encoding = Encoding.JSON;
    return this;
  }

  /** Sets the encoding of payloads to be Protobuf format. If unset defaults to Protobuf format. */
  public OtlpHttpSpanExporterBuilder setProtobufEncoding() {
    this.encoding = Encoding.PROTOBUF;
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently the
   * only supported compression method is "gzip".
   */
  public OtlpHttpSpanExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    Preconditions.checkArgument(
        "gzip".equals(compressionMethod),
        "Unsupported compression method. Supported compression methods include: gzip.");
    this.isCompressionEnabled = true;
    return this;
  }

  /** Add header to requests. */
  public OtlpHttpSpanExporterBuilder addHeader(String key, String value) {
    if (headersBuilder == null) {
      headersBuilder = new Headers.Builder();
    }
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
        new OkHttpClient.Builder()
            .callTimeout(Duration.ofNanos(timeoutNanos));
    if (isCompressionEnabled) {
      clientBuilder.addInterceptor(OtlpHttpUtil.GZIP_INTERCEPTOR);
    }

    if (trustedCertificatesPem != null) {
      try {
        HandshakeCertificates handshakeCertificates =
            OtlpHttpUtil.toHandshakeCertificates(trustedCertificatesPem);
        clientBuilder.sslSocketFactory(
            handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager());
      } catch (CertificateException e) {
        throw new IllegalStateException(
            "Could not set trusted certificate for OTLP HTTP connection, are they valid X.509 in PEM format?",
            e);
      }
    }

    RequestResponseHandler requestResponseHandler =
        encoding == Encoding.JSON
            ? OtlpHttpUtil.JSON_REQUEST_RESPONSE_HANDLER
            : OtlpHttpUtil.PROTOBUF_REQUEST_RESPONSE_HANDLER;

    Headers headers = headersBuilder == null ? null : headersBuilder.build();

    return new OtlpHttpSpanExporter(
        clientBuilder.build(), endpoint, headers, requestResponseHandler);
  }

  enum Encoding {
    JSON,
    PROTOBUF
  }

  OtlpHttpSpanExporterBuilder() {}
}
