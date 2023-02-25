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

/**
 * Utility class to help with management of TLS related components. This class is ultimately
 * responsible for enabling TLS via callbacks passed to the configure[...]() methods. This class is
 * only intended for internal OpenTelemetry exporter usage and should not be used by end-users.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
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
  public TlsConfigHelper createTrustManager(byte[] trustedCertsPem) {
    try {
      this.trustManager = TlsUtil.trustManager(trustedCertsPem);
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating X509TrustManager with provided certs. Are they valid X.509 in PEM format?",
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
  public TlsConfigHelper createKeyManager(byte[] privateKeyPem, byte[] certificatePem) {
    try {
      if (keyManager != null) {
        logger.warning(
            "Previous X509 Key manager is being replaced. This is probably an error and should only be set once.");
      }
      keyManager = TlsUtil.keyManager(privateKeyPem, certificatePem);
      return this;
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating X509KeyManager with provided certs. Are they valid X.509 in PEM format?",
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

  /**
   * Functional wrapper type used in configure methods. Exists primarily to declare checked
   * SSLException.
   */
  public interface SslSocketFactoryConfigurer {
    void configure(SSLSocketFactory sslSocketFactory, X509TrustManager trustManager)
        throws SSLException;
  }

  /**
   * Functional wrapper type used in configure methods. Exists primarily to declare checked
   * SSLException.
   */
  public interface KeyManagerConfigurer {
    void configure(X509TrustManager trustManager, X509KeyManager keyManager) throws SSLException;
  }

  /**
   * Configures TLS by invoking the given callback with the X509TrustManager and X509KeyManager. If
   * the trust manager or key manager have not yet been configured, this method does nothing.
   */
  public void configureWithKeyManager(KeyManagerConfigurer configureMethod) {
    if (trustManager == null || keyManager == null) {
      return;
    }
    try {
      configureMethod.configure(trustManager, keyManager);
    } catch (SSLException e) {
      wrapException(e);
    }
  }

  /**
   * Configures TLS by invoking the provided consumer with a new SSLSocketFactory and the
   * preconfigured X509TrustManager. If the trust manager has not been configured, this method is
   * effectively a noop.
   */
  public void configureWithSocketFactory(SslSocketFactoryConfigurer configureMethod) {
    if (trustManager == null) {
      warnIfOtherComponentsConfigured();
      return;
    }

    try {
      SSLSocketFactory sslSocketFactory = this.sslSocketFactory;
      if (sslSocketFactory == null) {
        sslSocketFactory = TlsUtil.sslSocketFactory(keyManager, trustManager);
      }
      configureMethod.configure(sslSocketFactory, trustManager);
    } catch (SSLException e) {
      wrapException(e);
    }
  }

  private static void wrapException(SSLException e) {
    throw new IllegalStateException(
        "Could not configure TLS connection, are certs in valid X.509 in PEM format?", e);
  }

  private void warnIfOtherComponentsConfigured() {
    if (sslSocketFactory != null) {
      logger.warning("sslSocketFactory has been configured without an X509TrustManager.");
      return;
    }
    if (keyManager != null) {
      logger.warning("An X509KeyManager has been configured without an X509TrustManager.");
    }
  }
}
