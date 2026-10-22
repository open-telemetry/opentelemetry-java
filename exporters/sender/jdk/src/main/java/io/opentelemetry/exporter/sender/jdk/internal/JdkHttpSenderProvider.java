/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.HttpSenderConfig;
import io.opentelemetry.sdk.common.export.HttpSenderProvider;
import java.net.URI;

/**
 * {@link HttpSender} SPI implementation for {@link JdkHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JdkHttpSenderProvider implements HttpSenderProvider {

  @Override
  public HttpSender createSender(HttpSenderConfig httpSenderConfig) {
    URI endpoint = httpSenderConfig.getEndpoint();
    // HttpRequest.Builder#uri rejects a URI without a host. URI#getHost() returns null for some
    // valid DNS names (JDK-8188305), which EndpointUtil accepts, so fail here with a clear message
    // instead of on the first export.
    if (endpoint.getHost() == null) {
      throw new IllegalArgumentException(
          "Invalid endpoint, host not supported by the JDK HttpClient: "
              + endpoint
              + ". java.net.URI cannot parse host names whose last label starts with a digit."
              + " Use the OkHttp sender or a fully qualified host name.");
    }
    return new JdkHttpSender(
        endpoint,
        httpSenderConfig.getContentType(),
        httpSenderConfig.getCompressor(),
        httpSenderConfig.getTimeout(),
        httpSenderConfig.getConnectTimeout(),
        httpSenderConfig.getHeadersSupplier(),
        httpSenderConfig.getRetryPolicy(),
        httpSenderConfig.getProxyOptions(),
        httpSenderConfig.getSslContext(),
        httpSenderConfig.getExecutorService(),
        httpSenderConfig.getMaxResponseBodySize(),
        httpSenderConfig.getEnabledProtocols());
  }
}
