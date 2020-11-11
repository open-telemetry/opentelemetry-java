/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.aws.resource;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

class JdkHttpClient {

  private static final Logger logger = Logger.getLogger(JdkHttpClient.class.getName());

  private static final int TIMEOUT_MILLIS = 2000;

  String fetchString(
      String httpMethod, String urlStr, Map<String, String> requestPropertyMap, String certPath) {
    final HttpURLConnection connection;

    try {
      if (urlStr.startsWith("https")) {
        connection = (HttpURLConnection) new URL(urlStr).openConnection();
        KeyStore keyStore = getKeystoreForTrustedCert(certPath);
        if (keyStore != null) {
          ((HttpsURLConnection) connection).setSSLSocketFactory(buildSslSocketFactory(keyStore));
        }
      } else {
        connection = (HttpURLConnection) new URL(urlStr).openConnection();
      }

      connection.setRequestMethod(httpMethod);
      connection.setConnectTimeout(TIMEOUT_MILLIS);
      connection.setReadTimeout(TIMEOUT_MILLIS);

      for (Map.Entry<String, String> requestProperty : requestPropertyMap.entrySet()) {
        connection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
      }

      int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        logger.log(
            Level.FINE,
            "Error reponse from "
                + urlStr
                + " code ("
                + responseCode
                + ") text "
                + readResponseString(connection));
        return "";
      }

      return readResponseString(connection).trim();

    } catch (IOException e) {
      logger.log(Level.FINE, "JdkHttpClient fetch string failed.", e);
    }

    return "";
  }

  private static String readResponseString(HttpURLConnection connection) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (InputStream is = connection.getInputStream()) {
      ByteStreams.copy(is, os);
    } catch (IOException e) {
      // Only best effort read if we can.
    }
    try (InputStream is = connection.getErrorStream()) {
      if (is != null) {
        ByteStreams.copy(is, os);
      }
    } catch (IOException e) {
      // Only best effort read if we can.
    }
    try {
      return os.toString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      logger.log(Level.WARNING, "UTF-8 not supported can't happen.", e);
    }
    return "";
  }

  private static SSLSocketFactory buildSslSocketFactory(KeyStore keyStore) {
    try {
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);

      SSLContext context = SSLContext.getInstance("TLS");
      context.init(null, tmf.getTrustManagers(), null);
      return context.getSocketFactory();

    } catch (Exception e) {
      logger.log(Level.WARNING, "Build SslSocketFactory for K8s restful client exception.", e);
    }
    return null;
  }

  private static KeyStore getKeystoreForTrustedCert(String certPath) {
    try (FileInputStream fis = new FileInputStream(certPath)) {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(fis);

      int i = 0;
      for (Certificate certificate : certificates) {
        trustStore.setCertificateEntry("cert_" + i, certificate);
        i++;
      }
      return trustStore;
    } catch (Exception e) {
      logger.log(Level.WARNING, "Cannot load KeyStore from " + certPath);
      return null;
    }
  }
}
