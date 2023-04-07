/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility class to help with management of TLS related components. TLS config consists {@link
 * #keyManager}, {@link #trustManager}, which combine to form {@link #sslContext}. These components
 * can be configured via higher level APIs ({@link #createTrustManager(byte[])} and {@link
 * #createKeyManager(byte[], byte[])}) which parse keys in PEM format, or the lower level API {@link
 * #setSslContext(SSLContext, X509TrustManager)} in which the components are directly set, but NOT
 * both. Attempts to reconfigure components which have already been configured throw {@link
 * IllegalStateException}. Consumers access components via any combination of {@link
 * #getKeyManager()}, {@link #getTrustManager()}, and {@link #getSslContext()}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class TlsConfigHelper {

  @Nullable private X509KeyManager keyManager;
  @Nullable private X509TrustManager trustManager;
  @Nullable private SSLContext sslContext;

  public TlsConfigHelper() {}

  /**
   * Configure the {@link X509TrustManager} from the given cert content.
   *
   * <p>Must not be called multiple times, or if {@link #setSslContext(SSLContext,
   * X509TrustManager)} has been previously called.
   *
   * @param trustedCertsPem Certificate in PEM format.
   */
  public void createTrustManager(byte[] trustedCertsPem) {
    if (trustManager != null) {
      throw new IllegalStateException("trustManager has been previously configured");
    }

    try {
      this.trustManager = TlsUtil.trustManager(trustedCertsPem);
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating X509TrustManager with provided certs. Are they valid X.509 in PEM format?",
          e);
    }
  }

  /**
   * Configure the {@link X509KeyManager} from the given private key and certificate, both in PEM
   * format.
   *
   * <p>Must not be called multiple times, or if {@link #setSslContext(SSLContext,
   * X509TrustManager)} has been previously called.
   *
   * @param privateKeyPem Private key content in PEM format.
   * @param certificatePem Certificate content in PEM format.
   */
  public void createKeyManager(byte[] privateKeyPem, byte[] certificatePem) {
    if (keyManager != null) {
      throw new IllegalStateException("keyManager has been previously configured");
    }

    try {
      keyManager = TlsUtil.keyManager(privateKeyPem, certificatePem);
    } catch (SSLException e) {
      throw new IllegalStateException(
          "Error creating X509KeyManager with provided certs. Are they valid X.509 in PEM format?",
          e);
    }
  }

  /**
   * Configure the {@link SSLContext} and {@link X509TrustManager}.
   *
   * <p>Must not be called multiple times, or if {@link #createTrustManager(byte[])} or {@link
   * #createKeyManager(byte[], byte[])} has been previously called.
   *
   * @param sslContext the SSL context.
   * @param trustManager the trust manager.
   */
  public void setSslContext(SSLContext sslContext, X509TrustManager trustManager) {
    if (this.sslContext != null || this.trustManager != null) {
      throw new IllegalStateException("sslContext or trustManager has been previously configured");
    }
    this.trustManager = trustManager;
    this.sslContext = sslContext;
  }

  /** Get the {@link X509KeyManager}. */
  @Nullable
  public X509KeyManager getKeyManager() {
    return keyManager;
  }

  /** Get the {@link X509TrustManager}. */
  @Nullable
  public X509TrustManager getTrustManager() {
    return trustManager;
  }

  /** Get the {@link SSLContext}. */
  @Nullable
  public SSLContext getSslContext() {
    if (sslContext != null) {
      return sslContext;
    }

    try {
      SSLContext sslContext;
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(
          keyManager == null ? null : new KeyManager[] {keyManager},
          trustManager == null ? null : new TrustManager[] {trustManager},
          null);
      return sslContext;
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
