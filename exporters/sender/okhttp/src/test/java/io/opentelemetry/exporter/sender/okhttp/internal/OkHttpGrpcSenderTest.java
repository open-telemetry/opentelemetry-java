package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import java.util.Collections;

class OkHttpGrpcSenderTest
    extends OkHttpSenderTest<GrpcSender<OkHttpGrpcSenderTest.DummyMarshaler>> {

  @Override
  void send(GrpcSender<DummyMarshaler> sender, Runnable onSuccess, Runnable onFailure) {
    sender.send(new DummyMarshaler(), onSuccess, (grpcResponse, throwable) -> onFailure.run());
  }

  @Override
  GrpcSender<DummyMarshaler> createSender(String endpoint) {
    return new OkHttpGrpcSender<>(
        "https://localhost", false, 10L, Collections.emptyMap(), null, null, null);
  }

  protected static class DummyMarshaler extends MarshalerWithSize {

    protected DummyMarshaler() {
      super(0);
    }

    @Override
    protected void writeTo(Serializer output) {}
  }
}
