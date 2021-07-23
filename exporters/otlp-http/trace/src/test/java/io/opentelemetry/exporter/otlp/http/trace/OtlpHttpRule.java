/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class OtlpHttpRule implements TestRule {

  private final HeldCertificate heldCertificate;
  private final MockWebServer server = new MockWebServer();
  private final OtlpHttpDispatcher dispatcher = new OtlpHttpDispatcher();

  OtlpHttpRule() {
    try {
      heldCertificate =
          new HeldCertificate.Builder()
              .commonName(server.getHostName())
              .addSubjectAlternativeName(
                  InetAddress.getByName(server.getHostName()).getCanonicalHostName())
              .build();
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Could not get canonical host name.", e);
    }
    server.setDispatcher(dispatcher);
    server.useHttps(
        new HandshakeCertificates.Builder()
            .heldCertificate(heldCertificate)
            .build()
            .sslSocketFactory(),
        false);
  }

  String certificatePem() {
    return heldCertificate.certificatePem();
  }

  String endpoint() {
    return String.format("https://%s:%s/v1/traces", server.getHostName(), server.getPort());
  }

  List<ExportTraceServiceRequest> getRequests() {
    return dispatcher.getRequests();
  }

  void addMockResponse(MockResponse mockResponse) {
    this.dispatcher.addMockResponse(mockResponse);
  }

  @NotNull
  @Override
  public Statement apply(@NotNull Statement base, @NotNull Description description) {
    return server.apply(base, description);
  }
}
