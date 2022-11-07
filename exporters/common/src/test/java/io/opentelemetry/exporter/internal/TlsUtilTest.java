/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.linecorp.armeria.internal.common.util.SelfSignedCertificate;
import java.security.KeyFactory;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TlsUtilTest {

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
}
