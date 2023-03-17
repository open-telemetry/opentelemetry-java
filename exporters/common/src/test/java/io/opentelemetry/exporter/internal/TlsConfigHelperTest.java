/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressLogger(TlsConfigHelper.class)
class TlsConfigHelperTest {
  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForLogger(TlsConfigHelper.class.getName());

  @Mock TlsConfigHelper.TlsUtility tlsUtil;

  @Test
  void createTrustManager_exceptionMapped() throws Exception {
    when(tlsUtil.trustManager(any())).thenThrow(new SSLException("boom"));
    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);
    assertThrows(
        IllegalStateException.class,
        () -> {
          helper.createTrustManager(serverTls.certificate().getEncoded());
        });
  }

  @Test
  void createKeymanager_exceptionMapped() throws Exception {
    when(tlsUtil.keyManager(any(), any())).thenThrow(new SSLException("boom"));
    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);
    assertThrows(
        IllegalStateException.class,
        () -> {
          helper.createKeyManager(
              serverTls.privateKey().getEncoded(), serverTls.certificate().getEncoded());
        });
  }

  @Test
  void socketFactory_happyPathWithBytes() throws Exception {

    byte[] cert = serverTls.certificate().getEncoded();
    byte[] key = serverTls.privateKey().getEncoded();
    AtomicReference<X509TrustManager> seenTrustManager = new AtomicReference<>();
    AtomicReference<SSLSocketFactory> seenSocketFactory = new AtomicReference<>();

    X509TrustManager trustManager = mock(X509TrustManager.class);
    X509KeyManager keyManager = mock(X509KeyManager.class);
    SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);

    when(tlsUtil.trustManager(cert)).thenReturn(trustManager);
    when(tlsUtil.keyManager(key, cert)).thenReturn(keyManager);
    when(tlsUtil.sslSocketFactory(keyManager, trustManager)).thenReturn(sslSocketFactory);

    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.createKeyManager(key, cert);
    helper.createTrustManager(cert);
    helper.configureWithSocketFactory(
        (sf, tm) -> {
          seenTrustManager.set(tm);
          seenSocketFactory.set(sf);
        });
    assertSame(trustManager, seenTrustManager.get());
    assertSame(sslSocketFactory, seenSocketFactory.get());
  }

  @Test
  void socketFactory_noTrustManager() {
    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);
    helper.configureWithSocketFactory(
        (sf, tm) -> {
          fail("Should not have called due to missing trust manager");
        });
    verifyNoInteractions(tlsUtil);
  }

  @Test
  void socketFactoryCallbackExceptionHandled() {
    X509TrustManager trustManager = mock(X509TrustManager.class);

    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.setTrustManager(trustManager);
    assertThrows(
        IllegalStateException.class,
        () -> {
          helper.configureWithSocketFactory(
              (sf, tm) -> {
                throw new SSLException("bad times");
              });
        });
  }

  @Test
  void configureWithKeyManagerHappyPath() {
    AtomicReference<X509TrustManager> seenTrustManager = new AtomicReference<>();
    AtomicReference<X509KeyManager> seenKeyManager = new AtomicReference<>();

    X509TrustManager trustManager = mock(X509TrustManager.class);
    X509KeyManager keyManager = mock(X509KeyManager.class);

    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.setTrustManager(trustManager);
    helper.setKeyManager(keyManager);
    helper.configureWithKeyManager(
        (tm, km) -> {
          seenTrustManager.set(tm);
          seenKeyManager.set(km);
        });
    assertThat(seenTrustManager.get()).isSameAs(trustManager);
    assertThat(seenKeyManager.get()).isSameAs(keyManager);
  }

  @Test
  void configureWithKeyManagerExceptionHandled() {
    X509TrustManager trustManager = mock(X509TrustManager.class);
    X509KeyManager keyManager = mock(X509KeyManager.class);

    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.setTrustManager(trustManager);
    helper.setKeyManager(keyManager);
    assertThrows(
        IllegalStateException.class,
        () ->
            helper.configureWithKeyManager(
                (trustManager1, keyManager1) -> {
                  throw new SSLException("ouchey");
                }));
  }

  @Test
  void configureWithKeyManagerNoTrustManager() {
    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);
    helper.configureWithKeyManager(
        (tm, km) -> {
          fail();
        });
    verifyNoInteractions(tlsUtil);
  }

  @Test
  void setKeyManagerReplacesAndWarns() {
    X509KeyManager keyManager1 = mock(X509KeyManager.class);
    X509KeyManager keyManager2 = mock(X509KeyManager.class);

    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.setTrustManager(mock(X509TrustManager.class));
    helper.setKeyManager(keyManager1);
    helper.setKeyManager(keyManager2);

    helper.configureWithKeyManager((tm, km) -> assertSame(km, keyManager2));
    logs.assertContains("Previous X509 Key manager is being replaced");
  }

  @Test
  void createKeyManagerReplacesAndWarns() throws Exception {
    X509KeyManager keyManager1 = mock(X509KeyManager.class);
    X509KeyManager keyManager2 = mock(X509KeyManager.class);

    byte[] cert = serverTls.certificate().getEncoded();
    byte[] key = serverTls.privateKey().getEncoded();

    when(tlsUtil.keyManager(key, cert)).thenReturn(keyManager2);
    TlsConfigHelper helper = new TlsConfigHelper(tlsUtil);

    helper.setTrustManager(mock(X509TrustManager.class));
    helper.setKeyManager(keyManager1);
    helper.createKeyManager(key, cert);

    helper.configureWithKeyManager((tm, km) -> assertSame(km, keyManager2));
    logs.assertContains("Previous X509 Key manager is being replaced");
  }
}
