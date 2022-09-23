/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfoBuilder;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class InstrumentationScopeUtil {

  /** Convert to {@link io.opentelemetry.sdk.common.InstrumentationLibraryInfo}. */
  @SuppressWarnings("deprecation") // Utility method for compatibility
  public static io.opentelemetry.sdk.common.InstrumentationLibraryInfo toInstrumentationLibraryInfo(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    return io.opentelemetry.sdk.common.InstrumentationLibraryInfo.create(
        instrumentationScopeInfo.getName(),
        instrumentationScopeInfo.getVersion(),
        instrumentationScopeInfo.getSchemaUrl());
  }

  /** Convert to {@link InstrumentationScopeInfo}. */
  @SuppressWarnings("deprecation") // Utility method for compatibility
  public static InstrumentationScopeInfo toInstrumentationScopeInfo(
      io.opentelemetry.sdk.common.InstrumentationLibraryInfo instrumentationLibraryInfo) {
    InstrumentationScopeInfoBuilder builder =
        InstrumentationScopeInfo.builder(instrumentationLibraryInfo.getName());
    if (instrumentationLibraryInfo.getVersion() != null) {
      builder.setVersion(instrumentationLibraryInfo.getVersion());
    }
    if (instrumentationLibraryInfo.getSchemaUrl() != null) {
      builder.setSchemaUrl(instrumentationLibraryInfo.getSchemaUrl());
    }
    return builder.build();
  }

  private InstrumentationScopeUtil() {}
}
