/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.export.TlsCompatibilityMode;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Tests TLS compatibility modes against a server that only supports legacy TLS protocols.
 *
 * <p>OkHttp's {@code ConnectionSpec.MODERN_TLS} and {@code ConnectionSpec.COMPATIBLE_TLS} differ in
 * the supported TLS protocol versions.
 *
 * <p>The test server is restricted to TLSv1/TLSv1.1 because these are the protocol versions that
 * distinguish MODERN_TLS from COMPATIBLE_TLS.
 */

// This test temporarily changes the JVM-wide TLS disabled algorithms to enable legacy TLS protocol
// testing, so it must not run concurrently with other tests.
@Isolated
class OkHttpHttpSenderTlsCompatibilityTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  private static EventLoopGroup bossGroup;
  private static EventLoopGroup workerGroup;
  private static Channel serverChannel;
  private static URI serverUri;

  @Nullable private static String previousDisabledAlgorithms;

  @BeforeAll
  static void setup() throws Exception {
    String disabledAlgorithms = Security.getProperty("jdk.tls.disabledAlgorithms");

    previousDisabledAlgorithms = disabledAlgorithms;

    if (disabledAlgorithms
        != null) { // remove (TLSv1, TLSv1.1) from the disabled algorithms so we can test legacy
      // protocols
      String updatedAlgorithms =
          Arrays.stream(disabledAlgorithms.split(","))
              .map(String::trim)
              .filter(algorithm -> !algorithm.equals("TLSv1") && !algorithm.equals("TLSv1.1"))
              .collect(Collectors.joining(", "));

      Security.setProperty("jdk.tls.disabledAlgorithms", updatedAlgorithms);
    }

    bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());

    workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

    SslContext sslContext =
        SslContextBuilder.forServer(certificate.privateKey(), certificate.certificate())
            .protocols("TLSv1", "TLSv1.1")
            .applicationProtocolConfig(ApplicationProtocolConfig.DISABLED)
            .build();

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<SocketChannel>() {

              @Override
              protected void initChannel(SocketChannel ch) {

                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(sslContext.newHandler(ch.alloc()));

                pipeline.addLast(new HttpServerCodec());

                pipeline.addLast(new HttpObjectAggregator(1024 * 1024));

                pipeline.addLast(
                    new SimpleChannelInboundHandler<FullHttpRequest>() {

                      @Override
                      protected void channelRead0(
                          ChannelHandlerContext ctx, FullHttpRequest request) {

                        FullHttpResponse response =
                            new DefaultFullHttpResponse(
                                HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

                        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                      }
                    });
              }
            });

    serverChannel = bootstrap.bind(0).sync().channel();

    int port = ((InetSocketAddress) serverChannel.localAddress()).getPort();

    serverUri = URI.create("https://localhost:" + port + "/");
  }

  @AfterAll
  static void restoreDisabledAlgorithms() throws Exception {
    if (serverChannel != null) {
      serverChannel.close().sync();
    }

    if (bossGroup != null) {
      bossGroup.shutdownGracefully().sync();
    }

    if (workerGroup != null) {
      workerGroup.shutdownGracefully().sync();
    }
    if (previousDisabledAlgorithms == null) {
      Security.setProperty("jdk.tls.disabledAlgorithms", null);
    } else {
      Security.setProperty("jdk.tls.disabledAlgorithms", previousDisabledAlgorithms);
    }
  }

  @Test
  void modernTls_cannotReachLegacyProtocolOnlyServer() throws Exception {
    // If this JVM's TLS provider does not implement TLSv1/1.1, skip instead of
    // failing for an unrelated reason.
    assumeTrue(
        supportsProtocol("TLSv1") || supportsProtocol("TLSv1.1"),
        "TLSv1/TLSv1.1 are not supported by this JVM");

    OkHttpHttpSender sender = buildSender(TlsCompatibilityMode.MODERN);

    Result result = send(sender);

    assertThat(result.response).isNull();
    assertThat(result.error)
        .isNotNull(); // handshake failure: no protocol version in common with the server
  }

  @Test
  void compatibleTls_reachesLegacyProtocolOnlyServer() throws Exception {
    assumeTrue(
        supportsProtocol("TLSv1") || supportsProtocol("TLSv1.1"),
        "TLSv1/TLSv1.1 are not supported by this JVM");

    OkHttpHttpSender sender = buildSender(TlsCompatibilityMode.COMPATIBLE);

    Result result = send(sender);

    assertThat(result.error).isNull();
    assertThat(result.response).isNotNull();
    assertThat(result.response.getStatusCode()).isEqualTo(200);
  }

  private static OkHttpHttpSender buildSender(TlsCompatibilityMode tlsCompatibilityMode)
      throws Exception {
    X509TrustManager trustManager = TlsUtil.trustManager(certificate.certificate().getEncoded());
    SSLContext sslContext = SSLContext.getInstance("TLS");
    // Use the same trust configuration in both tests so only the TLS compatibility
    // mode differs.
    sslContext.init(null, new TrustManager[] {trustManager}, null);

    return new OkHttpHttpSender(
        serverUri,
        "text/plain",
        null,
        Duration.ofSeconds(10),
        Duration.ofSeconds(10),
        Collections::emptyMap,
        null,
        null,
        sslContext,
        trustManager,
        null,
        Long.MAX_VALUE,
        tlsCompatibilityMode);
  }

  private static Result send(OkHttpHttpSender sender) throws InterruptedException {
    AtomicReference<HttpResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    sender.send(
        new NoOpRequestBodyWriter(),
        response -> {
          responseRef.set(response);
          latch.countDown();
        },
        error -> {
          errorRef.set(error);
          latch.countDown();
        });

    assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    return new Result(responseRef.get(), errorRef.get());
  }

  private static boolean supportsProtocol(String protocol) {
    try {
      SSLContext.getInstance(protocol);
      return true;
    } catch (NoSuchAlgorithmException e) {
      return false;
    }
  }

  private static class Result {
    @Nullable final HttpResponse response;
    @Nullable final Throwable error;

    Result(@Nullable HttpResponse response, @Nullable Throwable error) {
      this.response = response;
      this.error = error;
    }
  }

  private static class NoOpRequestBodyWriter implements MessageWriter {
    @Override
    public void writeMessage(OutputStream output) {}

    @Override
    public int getContentLength() {
      return 0;
    }
  }
}
