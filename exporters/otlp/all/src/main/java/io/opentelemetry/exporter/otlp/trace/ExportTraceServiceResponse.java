/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

// A Java object to correspond to the gRPC response for the TraceService.Export method. If fields
// are added to the type in the future, this can be converted to an actual class.
//
// It may seem like Void could be used instead but gRPC does not allow response values to be
// null.
enum ExportTraceServiceResponse {
  INSTANCE;
}
