/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import java.io.IOException;

public interface Marshaler {
  void writeTo(CodedOutputStream output) throws IOException;

  int getSerializedSize();
}
