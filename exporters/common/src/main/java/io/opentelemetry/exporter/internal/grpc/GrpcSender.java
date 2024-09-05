/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.function.Consumer;

/**
 * An exporter of a messages encoded by {@link Marshaler} using the gRPC wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface GrpcSender<T extends Marshaler> {

  void send(T request, Consumer<GrpcResponse> onResponse, Consumer<Throwable> onError);

  /** Shutdown the sender. */
  CompletableResultCode shutdown();
}
