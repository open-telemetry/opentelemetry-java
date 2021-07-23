/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logging.data.LoggingData;
import java.util.Collection;

public interface LoggingExporter {

  CompletableResultCode export(Collection<LoggingData> metrics);

  CompletableResultCode flush();

  CompletableResultCode shutdown();
}
