/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;

public abstract class MarshalerServiceStub<T, U, S extends MarshalerServiceStub<T, U, S>>
    extends io.grpc.stub.AbstractFutureStub<S> {
  protected MarshalerServiceStub(Channel channel, CallOptions callOptions) {
    super(channel, callOptions);
  }

  public abstract ListenableFuture<U> export(T request);
}
