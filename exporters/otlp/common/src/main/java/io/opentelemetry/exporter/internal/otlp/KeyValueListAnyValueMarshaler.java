/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.*;
import io.opentelemetry.exporter.internal.otlp.metrics.MarshallingObjectsPool;
import io.opentelemetry.extension.incubator.logs.KeyAnyValue;
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValueList;
import io.opentelemetry.sdk.internal.DynamicList;
import java.io.IOException;
import java.util.List;

final class KeyValueListAnyValueMarshaler extends MarshalerWithSize {

  private final Marshaler value;

  private KeyValueListAnyValueMarshaler(KeyValueListMarshaler value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize create(List<KeyAnyValue> values) {
    int len = values.size();
    KeyValueMarshaler[] marshalers = new KeyValueMarshaler[values.size()];
    for (int i = 0; i < len; i++) {
      marshalers[i] = KeyValueMarshaler.createForKeyAnyValue(values.get(i));
    }
    return new KeyValueListAnyValueMarshaler(new KeyValueListMarshaler(marshalers));
  }

  public static MessageSize messageSize(List<KeyAnyValue> values, MarshallingObjectsPool pool) {
    MessageSize keyValueListMessageSize = KeyValueListMarshaler.messageSize(values, pool);
    int encodedSize = MarshalerUtil.sizeMessage(AnyValue.KVLIST_VALUE, keyValueListMessageSize);

    DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
    messageSize.set(encodedSize, DynamicList.of(keyValueListMessageSize));
    return messageSize;
  }

  public static void encode(
      Serializer output,
      List<KeyAnyValue> values,
      MessageSize keyValueListAnyValueMessageSize) throws IOException {
    output.serializeMessage(
        AnyValue.KVLIST_VALUE,
        values,
        (Serializer serializer, Object messageObject, MessageSize messageSize) ->
            KeyValueListMarshaler.encode(
                serializer, (List<KeyAnyValue>) messageObject, messageSize),
        keyValueListAnyValueMessageSize.getMessageTypedFieldSize(0));
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(AnyValue.KVLIST_VALUE, value);
  }

  private static int calculateSize(Marshaler value) {
    return MarshalerUtil.sizeMessage(AnyValue.KVLIST_VALUE, value);
  }

  private static class KeyValueListMarshaler extends MarshalerWithSize {

    private final Marshaler[] values;

    private KeyValueListMarshaler(KeyValueMarshaler[] values) {
      super(calculateSize(values));
      this.values = values;
    }

    public static MessageSize messageSize(List<KeyAnyValue> values, MarshallingObjectsPool pool) {
      DynamicList<MessageSize> valuesMessageSize = pool.borrowDynamicList(values.size());
      for (int i = 0; i < values.size(); i++) {
        KeyAnyValue value = values.get(i);
        valuesMessageSize.set(i, KeyValueMarshaler.messageSize(value, pool));
      }

      int encodedSize = MarshalerUtil.sizeRepeatedMessage(KeyValueList.VALUES, valuesMessageSize);
      DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
      messageSize.set(encodedSize, valuesMessageSize);
      return messageSize;
    }

    public static void encode(
        Serializer output,
        List<KeyAnyValue> values,
        MessageSize keyValueListMessageSize) throws IOException {
      output.serializeRepeatedMessage(
          KeyValueList.VALUES,
          values,
          (Serializer serializer, Object messageObject, MessageSize messageSize) ->
              KeyValueMarshaler.encode(serializer, (KeyAnyValue) messageObject, messageSize),
          keyValueListMessageSize.getMessageTypedFieldSizes());
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(KeyValueList.VALUES, values);
    }

    private static int calculateSize(Marshaler[] values) {
      return MarshalerUtil.sizeRepeatedMessage(KeyValueList.VALUES, values);
    }
  }
}
