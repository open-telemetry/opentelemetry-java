/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.internal.SamplingStrategyParameters;
import java.io.IOException;

final class SamplingStrategyParametersMarshaler extends MarshalerWithSize {

  private final byte[] serviceNameUtf8;

  static SamplingStrategyParametersMarshaler create(String serviceName) {
    return new SamplingStrategyParametersMarshaler(MarshalerUtil.toBytes(serviceName));
  }

  private SamplingStrategyParametersMarshaler(byte[] serviceName) {
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
