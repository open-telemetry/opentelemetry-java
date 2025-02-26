/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.http.HttpSenderConfig;
import io.opentelemetry.exporter.internal.http.HttpSenderProvider;

/**
 * {@link HttpSender} SPI implementation for {@link OkHttpHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSenderProvider implements HttpSenderProvider {

  @Override
  public HttpSender createSender(HttpSenderConfig httpSenderConfig) {
    return new OkHttpHttpSender(
        httpSenderConfig.getEndpoint(),
        httpSenderConfig.getCompressor(),
        httpSenderConfig.getExportAsJson(),
        httpSenderConfig.getContentType(),
        httpSenderConfig.getTimeoutNanos(),
        httpSenderConfig.getConnectTimeoutNanos(),
        httpSenderConfig.getHeadersSupplier(),
        httpSenderConfig.getProxyOptions(),
        httpSenderConfig.getRetryPolicy(),
        httpSenderConfig.getSslContext(),
        httpSenderConfig.getTrustManager(),
        httpSenderConfig.getExecutorService());
  }
}
