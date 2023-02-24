/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 * Utility class to help with management of TLS related components. This class is ultimately
 * responsible for enabling TLS with the OkHttpClientBuilder by setting the sslSocketFactory. This
 * class is only intended for internal OpenTelemetry exporter usage and should not be used by
 * end-users.
 */
public class TlsConfigHelper {

  private static final Logger logger = Logger.getLogger(TlsConfigHelper.class.getName());

  @Nullable private X509KeyManager keyManager;
  @Nullable private X509TrustManager trustManager;
  @Nullable private SSLSocketFactory sslSocketFactory;

  public TlsConfigHelper setTrustManager(X509TrustManager trustManager) {
    this.trustManager = trustManager;
    return this;
  }

  /**
   * Creates a new X509TrustManager from the given cert content.
   *
   * @param trustedCertsPem Certificate in PEM format.
   * @return this
   */
  public TlsConfigHelper configureTrustManager(byte[] trustedCertsPem) {
    try {
      this.trustManager = TlsUtil.trustManager(trustedCertsPem);
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating trust manager for OTLP HTTP connection with provided certs. Are they valid X.509 in PEM format?",
          e);
    }
    return this;
  }

  /**
   * Creates a new X509KeyManager from the given private key and certificate, both in PEM format.
   *
   * @param privateKeyPem Private key content in PEM format.
   * @param certificatePem Certificate content in PEM format.
   * @return this
   */
  public TlsConfigHelper configureKeyManager(byte[] privateKeyPem, byte[] certificatePem) {
    try {
      if (keyManager != null) {
        logger.warning(
            "Previous X509 Key manager is being replaced. This is probably an error and should only be set once.");
      }
      keyManager = TlsUtil.keyManager(privateKeyPem, certificatePem);
      return this;
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating key manager for TLS configuration of OTLP HTTP connection with provided certs. Are they valid X.509 in PEM format?",
          e);
    }
  }

  /**
   * Assigns the X509KeyManager.
   *
   * @return this
   */
  public TlsConfigHelper setKeyManager(X509KeyManager keyManager) {
    if (this.keyManager != null) {
      logger.warning(
          "Previous X509 Key manager is being replaced. This is probably an error and should only be set once.");
    }
    this.keyManager = keyManager;
    return this;
  }

  public TlsConfigHelper setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
    return this;
  }

  /** Configures TLS for the provided OkHttp client builder by setting the SSL Socket Factory. */
  public void configure(OkHttpClient.Builder clientBuilder) {
    if (trustManager == null) {
      if (warnIfOtherComponentsConfigured()) {
        logger.warning("An X509TrustManager must be configured for TLS to be configured.");
      }
      return;
    }

    try {
      SSLSocketFactory sslSocketFactory = this.sslSocketFactory;
      if (sslSocketFactory == null) {
        sslSocketFactory = TlsUtil.sslSocketFactory(keyManager, trustManager);
      }
      clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Could not set trusted certificate for OTLP HTTP connection, are they valid X.509 in PEM format?",
          e);
    }
  }

  private boolean warnIfOtherComponentsConfigured() {
    if (sslSocketFactory != null) {
      logger.warning("sslSocketFactory has been configured without an X509TrustManager.");
      return true;
    }
    if (keyManager != null) {
      logger.warning("An X509KeyManager has been configured without an X509TrustManager.");
      return true;
    }
    return false;
  }
}
