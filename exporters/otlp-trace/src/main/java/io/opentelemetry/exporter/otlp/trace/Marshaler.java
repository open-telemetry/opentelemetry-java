/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import com.google.protobuf.CodedOutputStream;
import java.io.IOException;

interface Marshaler {
  void writeTo(CodedOutputStream output) throws IOException;

  int getSerializedSize();
}
