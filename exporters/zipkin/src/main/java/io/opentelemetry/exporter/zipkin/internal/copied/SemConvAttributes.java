/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Provides access to semantic convention attributes used within the SDK implementation. This avoids
 * having to pull in semantic conventions as a dependency, which would easily collide and conflict
 * with user-provided dependencies.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class SemConvAttributes {

  private SemConvAttributes() {}

  public static final AttributeKey<String> OTEL_COMPONENT_TYPE =
      AttributeKey.stringKey("otel.component.type");
  public static final AttributeKey<String> OTEL_COMPONENT_NAME =
      AttributeKey.stringKey("otel.component.name");
  public static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

  public static final AttributeKey<String> SERVER_ADDRESS =
      AttributeKey.stringKey("server.address");
  public static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");

  public static final AttributeKey<Long> RPC_GRPC_STATUS_CODE =
      AttributeKey.longKey("rpc.grpc.status_code");
  public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
      AttributeKey.longKey("http.response.status_code");

  public static final AttributeKey<String> OTEL_SPAN_PARENT_ORIGIN =
      AttributeKey.stringKey("otel.span.parent.origin");
  public static final AttributeKey<String> OTEL_SPAN_SAMPLING_RESULT =
      AttributeKey.stringKey("otel.span.sampling_result");
}
