/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.security.cert.CertificateEncodingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressLogger(TlsConfigHelper.class)
class TlsConfigHelperTest {
  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  private TlsConfigHelper helper = new TlsConfigHelper();

  @Test
  void createTrustManager() throws CertificateEncodingException {
    helper.createTrustManager(serverTls.certificate().getEncoded());

    assertThat(helper.getTrustManager()).isNotNull();
  }

  @Test
  void createTrustManager_AlreadyExists_Throws() throws Exception {
    helper.createTrustManager(serverTls.certificate().getEncoded());
    assertThatThrownBy(() -> helper.createTrustManager(serverTls.certificate().getEncoded()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trustManager has been previously configured");

    helper = new TlsConfigHelper();
    helper.setSslContext(
        SSLContext.getInstance("TLS"), TlsUtil.trustManager(serverTls.certificate().getEncoded()));
    assertThatThrownBy(() -> helper.createTrustManager(serverTls.certificate().getEncoded()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("trustManager has been previously configured");
  }

  @Test
  void createKeyManager() throws CertificateEncodingException {
    helper.createKeyManager(
        serverTls.privateKey().getEncoded(), serverTls.certificate().getEncoded());

    assertThat(helper.getKeyManager()).isNotNull();
  }

  @Test
  void createKeyManager_AlreadyExists_Throws() throws Exception {
    helper.createKeyManager(
        serverTls.privateKey().getEncoded(), serverTls.certificate().getEncoded());
    assertThatThrownBy(
            () ->
                helper.createKeyManager(
                    serverTls.privateKey().getEncoded(), serverTls.certificate().getEncoded()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("keyManager has been previously configured");
  }

  @Test
  void setSslContext() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    X509TrustManager trustManager = TlsUtil.trustManager(serverTls.certificate().getEncoded());
    helper.setSslContext(sslContext, trustManager);

    assertThat(helper.getSslContext()).isEqualTo(sslContext);
    assertThat(helper.getTrustManager()).isEqualTo(trustManager);
  }

  @Test
  void setSslContext_AlreadyExists_Throws() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    X509TrustManager trustManager = TlsUtil.trustManager(serverTls.certificate().getEncoded());

    helper.setSslContext(sslContext, trustManager);
    assertThatThrownBy(() -> helper.setSslContext(sslContext, trustManager))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("sslContext or trustManager has been previously configured");

    helper = new TlsConfigHelper();
    helper.createTrustManager(serverTls.certificate().getEncoded());
    assertThatThrownBy(() -> helper.setSslContext(sslContext, trustManager))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("sslContext or trustManager has been previously configured");
  }
}
