/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.DefaultMessageSize;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.MarshallingObjectsPool;
import io.opentelemetry.exporter.internal.marshal.MessageSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
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

    DynamicList<MessageSize> messageFieldSizes = pool.borrowDynamicList(1);
    messageFieldSizes.add(keyValueListMessageSize);

    messageSize.set(encodedSize, messageFieldSizes);

    return messageSize;
  }

  @SuppressWarnings("unchecked")
  public static void encode(
      Serializer output,
      List<KeyAnyValue> values,
      MessageSize keyValueListAnyValueMessageSize,
      MarshallingObjectsPool pool)
      throws IOException {
    output.serializeMessage(
        AnyValue.KVLIST_VALUE,
        values,
        (Serializer serializer,
            Object messageObject,
            MessageSize messageSize,
            MarshallingObjectsPool marshallingObjectsPool) ->
            KeyValueListMarshaler.encode(
                serializer, (List<KeyAnyValue>) messageObject, messageSize, marshallingObjectsPool),
        keyValueListAnyValueMessageSize.getMessageTypeFieldSize(0),
        pool);
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

    public static MessageSize messageSize(
        List<KeyAnyValue> keyAnyValues, MarshallingObjectsPool pool) {
      DynamicList<MessageSize> keyAnyValuesMessageSize =
          pool.borrowDynamicList(keyAnyValues.size());
      for (int i = 0; i < keyAnyValues.size(); i++) {
        KeyAnyValue keyAnyValue = keyAnyValues.get(i);
        keyAnyValuesMessageSize.set(i, KeyValueMarshaler.messageSize(keyAnyValue, pool));
      }

      int encodedSize =
          MarshalerUtil.sizeRepeatedMessage(KeyValueList.VALUES, keyAnyValuesMessageSize);
      DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
      messageSize.set(encodedSize, keyAnyValuesMessageSize);
      return messageSize;
    }

    public static void encode(
        Serializer output,
        List<KeyAnyValue> values,
        MessageSize keyValueListMessageSize,
        MarshallingObjectsPool pool)
        throws IOException {
      output.serializeRepeatedMessage(
          KeyValueList.VALUES,
          values,
          (Serializer serializer,
              Object messageObject,
              MessageSize messageSize,
              MarshallingObjectsPool marshallingObjectsPool) ->
              KeyValueMarshaler.encode(
                  serializer, (KeyAnyValue) messageObject, messageSize, marshallingObjectsPool),
          keyValueListMessageSize.getMessageTypedFieldSizes(),
          pool);
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
