/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import java.util.Collections;

class OkHttpGrpcSuppressionTest
    extends AbstractOkHttpSuppressionTest<
        OkHttpGrpcSender<OkHttpGrpcSuppressionTest.DummyMarshaler>> {

  @Override
  void send(OkHttpGrpcSender<DummyMarshaler> sender, Runnable onSuccess, Runnable onFailure) {
    sender.send(new DummyMarshaler(), grpcResponse -> {}, throwable -> onFailure.run());
  }

  @Override
  OkHttpGrpcSender<DummyMarshaler> createSender(String endpoint) {
    return new OkHttpGrpcSender<>(
        "https://localhost", null, 10L, 10L, Collections::emptyMap, null, null, null, null);
  }

  protected static class DummyMarshaler extends MarshalerWithSize {

    protected DummyMarshaler() {
      super(0);
    }

    @Override
    protected void writeTo(Serializer output) {}
  }
}
