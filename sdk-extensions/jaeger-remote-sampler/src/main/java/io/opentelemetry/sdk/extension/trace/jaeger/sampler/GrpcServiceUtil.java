/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.Marshaler;
import java.net.URI;

class GrpcServiceUtil {

  static <ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler>
      GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> serviceBuilder(
          String type, long defaultTimeoutSecs, URI defaultEndpoint, String grpcEndpointPath) {

    return new OkHttpGrpcServiceBuilder<>(
        type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
  }

  private GrpcServiceUtil() {}
}
