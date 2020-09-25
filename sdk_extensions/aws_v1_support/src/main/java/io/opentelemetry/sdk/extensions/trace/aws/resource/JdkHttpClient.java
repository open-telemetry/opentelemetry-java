/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class JdkHttpClient {

  private static final Logger logger = Logger.getLogger(JdkHttpClient.class.getName());

  private static final int TIMEOUT_MILLIS = 2000;

  String fetchString(
      String httpMethod,
      String urlStr,
      Map<String, String> requestPropertyMap,
      String keyStorePath) {
    final HttpURLConnection connection;

    try {
      if (urlStr.startsWith("https")) {
        connection = (HttpsURLConnection) new URL(urlStr).openConnection();
        KeyStore keyStore = getK8sKeystore(keyStorePath);
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
      logger.log(Level.WARNING, "JdkHttpClient fetch string failed: " + e);
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
      logger.log(Level.WARNING, "UTF-8 not supported can't happen. " + e);
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
      logger.log(Level.WARNING, "Build SslSocketFactory for K8s restful client exception: " + e);
    }
    return null;
  }

  String getK8sCredHeader(String tokenFilePath) {
    try {
      File file = new File(tokenFilePath);
      String content = Files.asCharSource(file, Charsets.UTF_8).read();
      return "Bearer " + content;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Unable to load K8s client token: " + e);
    }
    return "";
  }

  private static KeyStore getK8sKeystore(String keyStorePath) {
    try (FileInputStream fis = new FileInputStream(keyStorePath)) {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(fis);

      for (Certificate certificate : certificates) {
        trustStore.setCertificateEntry("k8sca", certificate);
      }
      return trustStore;
    } catch (Exception e) {
      logger.log(Level.WARNING, "Cannot load KeyStore from " + keyStorePath);
      return null;
    }
  }
}
