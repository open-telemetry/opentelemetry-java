/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

public interface SdkTracerProviderConfigurer {
  void configure(SdkTracerProviderBuilder tracerProvider);
}
