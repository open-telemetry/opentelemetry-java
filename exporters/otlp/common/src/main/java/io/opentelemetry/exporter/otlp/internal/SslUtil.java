/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

public final class SslUtil {

  /**
   * Configure the channel builder to trust the certificates. The {@code byte[]} should contain an
   * X.509 certificate collection in PEM format.
   *
   * @throws SSLException if error occur processing the certificates
   */
  public static void setTrustedCertificatesPem(
      ManagedChannelBuilder<?> managedChannelBuilder, byte[] trustedCertificatesPem)
      throws SSLException {
    requireNonNull(managedChannelBuilder, "managedChannelBuilder");
    requireNonNull(trustedCertificatesPem, "trustedCertificatesPem");

    TrustManagerFactory tmf = trustManagerFactory(trustedCertificatesPem);

    // gRPC does not abstract TLS configuration so we need to check the implementation and act
    // accordingly.
    if (managedChannelBuilder.getClass().getName().equals("io.grpc.netty.NettyChannelBuilder")) {
      NettyChannelBuilder nettyBuilder = (NettyChannelBuilder) managedChannelBuilder;
      nettyBuilder.sslContext(GrpcSslContexts.forClient().trustManager(tmf).build());
    } else if (managedChannelBuilder
        .getClass()
        .getName()
        .equals("io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder")) {
      io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder nettyBuilder =
          (io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder) managedChannelBuilder;
      nettyBuilder.sslContext(
          io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forClient().trustManager(tmf).build());
    } else if (managedChannelBuilder
        .getClass()
        .getName()
        .equals("io.grpc.okhttp.OkHttpChannelBuilder")) {
      io.grpc.okhttp.OkHttpChannelBuilder okHttpBuilder =
          (io.grpc.okhttp.OkHttpChannelBuilder) managedChannelBuilder;
      SSLContext sslContext;
      try {
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
      } catch (NoSuchAlgorithmException e) {
        throw new SSLException("Could not get SSLContext for TLS.", e);
      } catch (KeyManagementException e) {
        throw new SSLException("Could not initialize SSLContext.", e);
      }
      okHttpBuilder.sslSocketFactory(sslContext.getSocketFactory());
    } else {
      throw new SSLException(
          "TLS certificate configuration not supported for unrecognized ManagedChannelBuilder "
              + managedChannelBuilder.getClass().getName());
    }
  }

  private static TrustManagerFactory trustManagerFactory(byte[] trustedCertificatesPem)
      throws SSLException {
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
      return tmf;
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      throw new SSLException("Could not build TrustManagerFactory from trustedCertificatesPem.", e);
    }
  }

  private SslUtil() {}
}
