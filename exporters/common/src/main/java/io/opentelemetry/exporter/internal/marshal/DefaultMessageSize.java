package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.exporter.internal.marshal.MessageSize;
import io.opentelemetry.sdk.internal.DynamicList;
import java.util.List;

public final class DefaultMessageSize implements MessageSize {

  private int encodedSize = 0;
  private DynamicList<MessageSize> messageFieldSizes = DynamicList.empty();

  DefaultMessageSize() {}

  public void set(int encodedSize) {
    this.encodedSize = encodedSize;
    this.messageFieldSizes = DynamicList.empty();
  }

  public void set(int encodedSize, DynamicList<MessageSize> messageFieldSizes) {
    this.encodedSize = encodedSize;
    this.messageFieldSizes = messageFieldSizes;
  }

  @Override
  public int getEncodedSize() {
    return encodedSize;
  }

  @Override
  public List<MessageSize> getMessageTypedFieldSizes() {
    return messageFieldSizes;
  }

  @Override
  public MessageSize getMessageTypedFieldSize(int messageFieldPosition) {
    return messageFieldSizes.get(messageFieldPosition);
  }
}
