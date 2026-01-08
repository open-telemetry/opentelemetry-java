/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.grpc.GrpcMessageWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

class OkHttpGrpcSuppressionTest extends AbstractOkHttpSuppressionTest<OkHttpGrpcSender> {

  @Override
  void send(OkHttpGrpcSender sender, Runnable onSuccess, Runnable onFailure) {
    sender.send(
        new GrpcMessageWriter() {
          @Override
          public void writeMessage(OutputStream output) throws IOException {}

          @Override
          public int getContentLength() {
            return 0;
          }
        },
        grpcResponse -> {},
        throwable -> onFailure.run());
  }

  @Override
  OkHttpGrpcSender createSender(String endpoint) {
    return new OkHttpGrpcSender(
        "https://localhost", null, 10L, 10L, Collections::emptyMap, null, null, null, null);
  }
}
