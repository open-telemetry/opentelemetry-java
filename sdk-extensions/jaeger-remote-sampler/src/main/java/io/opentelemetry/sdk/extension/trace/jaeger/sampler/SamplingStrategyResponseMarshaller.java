/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import java.io.IOException;

final class SamplingStrategyResponseMarshaller extends MarshalerWithSize {

  protected SamplingStrategyResponseMarshaller(int size) {
    super(size);
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {}
}
