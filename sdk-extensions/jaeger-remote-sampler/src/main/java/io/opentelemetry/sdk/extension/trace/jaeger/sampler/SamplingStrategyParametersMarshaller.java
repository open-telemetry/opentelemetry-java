/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.internal.SamplingStrategyParameters;
import java.io.IOException;

final class SamplingStrategyParametersMarshaller extends MarshalerWithSize {

  private final byte[] serviceNameUtf8;

  static SamplingStrategyParametersMarshaller create(String serviceName) {
    return new SamplingStrategyParametersMarshaller(MarshalerUtil.toBytes(serviceName));
  }

  private SamplingStrategyParametersMarshaller(byte[] serviceName) {
    super(calculateSize(serviceName));
    this.serviceNameUtf8 = serviceName;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeString(SamplingStrategyParameters.SERVICENAME, serviceNameUtf8);
  }

  private static int calculateSize(byte[] serviceName) {
    return MarshalerUtil.sizeBytes(SamplingStrategyParameters.SERVICENAME, serviceName);
  }
}
