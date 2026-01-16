/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.HttpSenderConfig;
import io.opentelemetry.sdk.common.export.HttpSenderProvider;

/**
 * {@link HttpSender} SPI implementation for {@link JdkHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JdkHttpSenderProvider implements HttpSenderProvider {

  @Override
  public HttpSender createSender(HttpSenderConfig httpSenderConfig) {
    return new JdkHttpSender(
        httpSenderConfig.getEndpoint(),
        httpSenderConfig.getContentType(),
        httpSenderConfig.getCompressor(),
        httpSenderConfig.getTimeout(),
        httpSenderConfig.getConnectTimeout(),
        httpSenderConfig.getHeadersSupplier(),
        httpSenderConfig.getRetryPolicy(),
        httpSenderConfig.getProxyOptions(),
        httpSenderConfig.getSslContext(),
        httpSenderConfig.getExecutorService());
  }
}
