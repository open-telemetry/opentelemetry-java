/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class InstrumentationScopeUtil {

  /** Convert to {@link InstrumentationLibraryInfo}. */
  public static InstrumentationLibraryInfo toInstrumentationLibraryInfo(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    return InstrumentationLibraryInfo.create(
        instrumentationScopeInfo.getName(),
        instrumentationScopeInfo.getVersion(),
        instrumentationScopeInfo.getSchemaUrl());
  }

  private InstrumentationScopeUtil() {}
}
