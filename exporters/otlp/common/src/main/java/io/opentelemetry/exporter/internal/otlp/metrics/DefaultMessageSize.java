package io.opentelemetry.exporter.internal.otlp.metrics;

public final class DefaultMessageSize implements MessageSize {

//  private final long size;
//  private final DynamicList<MessageSize> fieldSizes;

  @Override
  public long getEncodedSize() {
    return 0;
  }

  @Override
  public MessageSize getFieldSize(int fieldPosition) {
    return null;
  }
}
