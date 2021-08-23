package io.opentelemetry.exporter.otlp.internal;

enum NoopMarshaler implements Marshaler {
  INSTANCE;

  @Override
  public void writeTo(CodedOutputStream output) {
  }

  @Override
  public int getSerializedSize() {
    return 0;
  }
}
