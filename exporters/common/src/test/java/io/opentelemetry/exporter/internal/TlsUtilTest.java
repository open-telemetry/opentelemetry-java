/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.linecorp.armeria.internal.common.util.SelfSignedCertificate;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Stream;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TlsUtilTest {

  @TempDir private Path tempDir;

  private static final String EXPLANATORY_TEXT =
      "Subject: CN=Foo\n"
          + "Issuer: CN=Foo\n"
          + "Validity: from 7/9/2012 3:10:38 AM UTC to 7/9/2013 3:10:37 AM UTC\n";

  private SelfSignedCertificate rsaCertificate;
  private SelfSignedCertificate ecCertificate;

  @BeforeEach
  void setup() throws CertificateException {
    rsaCertificate =
        new SelfSignedCertificate(Date.from(Instant.now()), Date.from(Instant.now()), "RSA", 2048);
    ecCertificate =
        new SelfSignedCertificate(Date.from(Instant.now()), Date.from(Instant.now()), "EC", 256);
  }

  @Test
  void keyManager_Rsa() {
    assertThatCode(
            () ->
                TlsUtil.keyManager(
                    rsaCertificate.key().getEncoded(), rsaCertificate.cert().getEncoded()))
        .doesNotThrowAnyException();
  }

  @Test
  void keyManager_Ec() {
    assertThatCode(
            () ->
                TlsUtil.keyManager(
                    ecCertificate.key().getEncoded(), ecCertificate.cert().getEncoded()))
        .doesNotThrowAnyException();
  }

  @Test
  void generatePrivateKey_Invalid() {
    PKCS8EncodedKeySpec keySpec =
        new PKCS8EncodedKeySpec(TlsUtil.decodePem(rsaCertificate.key().getEncoded()));
    assertThatCode(
            () ->
                TlsUtil.generatePrivateKey(
                    keySpec, Collections.singletonList(KeyFactory.getInstance("EC"))))
        .isInstanceOf(SSLException.class)
        .hasMessage("Unable to generate key from supported algorithms: [EC]");
  }

  @Test
  void defaultTrustManager() {
    assertThatCode(TlsUtil::defaultTrustManager).doesNotThrowAnyException();
  }

  @Test
  void defaultTrustManager_returnsX509TrustManager() throws Exception {
    assertThat(TlsUtil.defaultTrustManager()).isInstanceOf(X509TrustManager.class);
  }

  private static TrustManagerFactory trustManagerFactory(TrustManager[] trustManagers) {
    return new TrustManagerFactory(
        new TrustManagerFactorySpi() {
          @Override
          protected void engineInit(KeyStore keyStore) {}

          @Override
          protected void engineInit(ManagerFactoryParameters spec) {}

          @Override
          protected TrustManager[] engineGetTrustManagers() {
            return trustManagers;
          }
        },
        null,
        "test") {};
  }

  @Test
  void defaultTrustManager_NoX509TrustManagerFound() {
    assertThatCode(() -> TlsUtil.defaultTrustManager(trustManagerFactory(new TrustManager[0])))
        .isInstanceOf(SSLException.class)
        .hasMessage("No X509TrustManager found");
  }

  /**
   * Append <a href="https://datatracker.ietf.org/doc/html/rfc7468#section-5.2">explanatory text</a>
   * prefix and verify {@link TlsUtil#keyManager(byte[], byte[])} succeeds.
   */
  @ParameterizedTest
  @MethodSource("keyManagerArgs")
  void keyManager_CertWithExplanatoryText(SelfSignedCertificate selfSignedCertificate)
      throws IOException {
    Path certificate = tempDir.resolve("certificate");
    Files.write(certificate, EXPLANATORY_TEXT.getBytes(StandardCharsets.UTF_8));
    Files.write(
        certificate,
        com.google.common.io.Files.toByteArray(selfSignedCertificate.certificate()),
        StandardOpenOption.APPEND);
    Files.write(certificate, "\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

    assertThatCode(
            () ->
                TlsUtil.keyManager(
                    com.google.common.io.Files.toByteArray(selfSignedCertificate.privateKey()),
                    com.google.common.io.Files.toByteArray(new File(certificate.toString()))))
        .doesNotThrowAnyException();
  }

  private static Stream<Arguments> keyManagerArgs() throws CertificateException {
    Instant now = Instant.now();
    return Stream.of(
        Arguments.argumentSet(
            "RSA certificate",
            new SelfSignedCertificate(Date.from(now), Date.from(now), "RSA", 2048)),
        Arguments.argumentSet(
            "EC certificate",
            new SelfSignedCertificate(Date.from(now), Date.from(now), "EC", 256)));
  }
}
