/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LogRecord;
import java.util.Collection;

/**
 * An exporter is responsible for taking a list of {@link LogRecord}s and transmitting them to their
 * ultimate destination.
 */
public interface LogExporter {
  CompletableResultCode export(Collection<LogRecord> records);

  CompletableResultCode shutdown();
}
