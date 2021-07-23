/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.oltp.logging;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LoggingData;
import io.opentelemetry.sdk.logging.export.LoggingExporter;
import java.util.Collection;

/** */
public class OltpGrpcLoggingExporter implements LoggingExporter {

  @Override
  public CompletableResultCode export(Collection<LoggingData> metrics) {
    return null;
  }

  @Override
  public CompletableResultCode flush() {
    return null;
  }

  @Override
  public CompletableResultCode shutdown() {
    return null;
  }
}
