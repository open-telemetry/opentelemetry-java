/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link LogRecord} with an associated {@link Resource} and {@link InstrumentationLibraryInfo}.
 */
@Immutable
public interface LogData extends LogRecord {

  /**
   * Returns the resource of this log.
   *
   * @return the resource.
   */
  Resource getResource();

  /**
   * Returns the instrumentation library that generated this log.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}.
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();
}
