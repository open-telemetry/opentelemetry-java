/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.internal.SdkConfigProvider;
import javax.annotation.Nullable;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

  static OpenTelemetrySdk createExtendedOpenTelemetrySdk(
      OpenTelemetrySdk openTelemetrySdk, @Nullable Object sdkConfigProvider) {
    SdkConfigProvider resolvedConfigProvider =
        sdkConfigProvider == null
            ? SdkConfigProvider.create(DeclarativeConfigProperties.empty())
            : (SdkConfigProvider) sdkConfigProvider;
    return ExtendedOpenTelemetrySdk.create(openTelemetrySdk, resolvedConfigProvider);
  }
}
