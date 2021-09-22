/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Utilities for working with TLS.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TlsUtil {

  /** Returns a {@link SSLSocketFactory} configured to use the given trust manager. */
  public static SSLSocketFactory sslSocketFactory(TrustManager trustManager) throws SSLException {

    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[] {trustManager}, null);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new SSLException(
          "Could not set trusted certificates for TLS connection, are they valid "
              + "X.509 in PEM format?",
          e);
    }
    return sslContext.getSocketFactory();
  }

  /** Returns a {@link TrustManager} for the given trusted certificates. */
  public static X509TrustManager trustManager(byte[] trustedCertificatesPem) throws SSLException {
    requireNonNull(trustedCertificatesPem, "trustedCertificatesPem");
    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null);

      ByteArrayInputStream is = new ByteArrayInputStream(trustedCertificatesPem);
      CertificateFactory factory = CertificateFactory.getInstance("X.509");
      int i = 0;
      while (is.available() > 0) {
        X509Certificate cert = (X509Certificate) factory.generateCertificate(is);
        ks.setCertificateEntry("cert_" + i, cert);
        i++;
      }

      TrustManagerFactory tmf =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
      return (X509TrustManager) tmf.getTrustManagers()[0];
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      throw new SSLException("Could not build TrustManagerFactory from trustedCertificatesPem.", e);
    }
  }

  private TlsUtil() {}
}
