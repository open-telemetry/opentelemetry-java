/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/**
 * Utilities for working with TLS.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TlsUtil {

  private static final String PEM_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
  private static final String PEM_KEY_FOOTER = "-----END PRIVATE KEY-----";

  private TlsUtil() {}

  /** Returns a {@link SSLSocketFactory} configured to use the given key and trust manager. */
  public static SSLSocketFactory sslSocketFactory(
      @Nullable KeyManager keyManager, TrustManager trustManager) throws SSLException {

    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("TLS");
      if (keyManager == null) {
        sslContext.init(null, new TrustManager[] {trustManager}, null);
      } else {
        sslContext.init(new KeyManager[] {keyManager}, new TrustManager[] {trustManager}, null);
      }
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new SSLException(
          "Could not set trusted certificates for TLS connection, are they valid "
              + "X.509 in PEM format?",
          e);
    }
    return sslContext.getSocketFactory();
  }

  /**
   * Creates {@link KeyManager} initiaded by keystore containing single private key with matching
   * certificate chain.
   */
  public static X509KeyManager keyManager(byte[] privateKeyPem, byte[] certificatePem)
      throws SSLException {
    requireNonNull(privateKeyPem, "privateKeyPem");
    requireNonNull(certificatePem, "certificatePem");
    try {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(null);
      KeyFactory factory = KeyFactory.getInstance("RSA");
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodePem(privateKeyPem));
      PrivateKey key = factory.generatePrivate(keySpec);

      CertificateFactory cf = CertificateFactory.getInstance("X.509");

      List<Certificate> chain = new ArrayList<>();

      ByteArrayInputStream is = new ByteArrayInputStream(certificatePem);
      while (is.available() > 0) {
        chain.add(cf.generateCertificate(is));
      }

      ks.setKeyEntry("trusted", key, "".toCharArray(), chain.toArray(new Certificate[] {}));

      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, "".toCharArray());
      return (X509KeyManager) kmf.getKeyManagers()[0];
    } catch (CertificateException
        | KeyStoreException
        | IOException
        | NoSuchAlgorithmException
        | UnrecoverableKeyException
        | InvalidKeySpecException e) {
      throw new SSLException("Could not build KeyManagerFactory from clientKeysPem.", e);
    }
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

  // We catch linkage error to provide a better exception message on Android.
  // https://github.com/open-telemetry/opentelemetry-java/issues/4533
  @IgnoreJRERequirement
  private static byte[] decodePem(byte[] pem) {
    String pemStr = new String(pem, StandardCharsets.UTF_8).trim();
    if (!pemStr.startsWith(PEM_KEY_HEADER) || !pemStr.endsWith(PEM_KEY_FOOTER)) {
      // pem may already be a decoded binary key, try to use it.
      return pem;
    }

    String contentWithNewLines =
        pemStr.substring(PEM_KEY_HEADER.length(), pemStr.length() - PEM_KEY_FOOTER.length());
    String content = contentWithNewLines.replaceAll("\\s", "");

    try {
      return Base64.getDecoder().decode(content);
    } catch (LinkageError unused) {
      throw new IllegalArgumentException(
          "PEM private keys are currently not supported on Android. "
              + "You may try a key encoded as DER.");
    }
  }
}
