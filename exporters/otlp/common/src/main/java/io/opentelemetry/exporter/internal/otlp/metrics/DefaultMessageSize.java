package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.sdk.internal.DynamicList;

public final class DefaultMessageSize implements MessageSize {

  private long encodedSize = 0;
  private DynamicList<MessageSize> messageFieldSizes = DynamicList.empty();

  DefaultMessageSize() {}

  public void set(long encodedSize) {
    this.encodedSize = encodedSize;
    this.messageFieldSizes = DynamicList.empty();
  }

  public void set(long encodedSize, DynamicList<MessageSize> messageFieldSizes) {
    this.encodedSize = encodedSize;
    this.messageFieldSizes = messageFieldSizes;
  }

  @Override
  public long getEncodedSize() {
    return encodedSize;
  }

  @Override
  public MessageSize getMessageTypedFieldSize(int messageFieldPosition) {
    return messageFieldSizes.get(messageFieldPosition);
  }
}
