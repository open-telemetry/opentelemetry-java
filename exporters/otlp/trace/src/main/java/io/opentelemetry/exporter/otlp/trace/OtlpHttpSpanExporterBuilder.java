/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import io.opentelemetry.internal.shaded.okhttp3.Headers;
import io.opentelemetry.internal.shaded.okhttp3.OkHttpClient;
import io.opentelemetry.internal.shaded.okhttp3.tls.HandshakeCertificates;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Builder utility for this exporter. */
public final class OtlpHttpSpanExporterBuilder {

  private static final long DEFAULT_TIMEOUT_SECS = 10;
  private static final String DEFAULT_ENDPOINT = "http://localhost:4317/v1/traces";

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private String endpoint = DEFAULT_ENDPOINT;
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
        new OkHttpClient.Builder().callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        HandshakeCertificates handshakeCertificates =
            toHandshakeCertificates(trustedCertificatesPem);
        clientBuilder.sslSocketFactory(
            handshakeCertificates.sslSocketFactory(), handshakeCertificates.trustManager());
      } catch (CertificateException e) {
        throw new IllegalStateException(
            "Could not set trusted certificate for OTLP HTTP connection, are they valid X.509 in PEM format?",
            e);
      }
    }

    Headers headers = headersBuilder == null ? null : headersBuilder.build();

    return new OtlpHttpSpanExporter(clientBuilder.build(), endpoint, headers, isCompressionEnabled);
  }

  /**
   * Extract X.509 certificates from the bytes.
   *
   * @param trustedCertificatesPem bytes containing an X.509 certificate collection in PEM format.
   * @return a HandshakeCertificates with the certificates
   * @throws CertificateException if an error occurs extracting certificates
   */
  private static HandshakeCertificates toHandshakeCertificates(byte[] trustedCertificatesPem)
      throws CertificateException {
    ByteArrayInputStream is = new ByteArrayInputStream(trustedCertificatesPem);
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    HandshakeCertificates.Builder certBuilder = new HandshakeCertificates.Builder();
    while (is.available() > 0) {
      X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
      certBuilder.addTrustedCertificate(cert);
    }
    return certBuilder.build();
  }

  OtlpHttpSpanExporterBuilder() {}
}
