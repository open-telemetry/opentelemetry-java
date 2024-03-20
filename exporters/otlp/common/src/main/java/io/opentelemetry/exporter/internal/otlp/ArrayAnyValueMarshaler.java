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
import io.opentelemetry.proto.common.v1.internal.AnyValue;
import io.opentelemetry.proto.common.v1.internal.ArrayValue;
import io.opentelemetry.sdk.internal.DynamicList;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

final class ArrayAnyValueMarshaler extends MarshalerWithSize {
  private final Marshaler value;

  private ArrayAnyValueMarshaler(ArrayValueMarshaler value) {
    super(calculateSize(value));
    this.value = value;
  }

  static MarshalerWithSize createAnyValue(
      List<io.opentelemetry.extension.incubator.logs.AnyValue<?>> values) {
    return createInternal(values, AnyValueMarshaler::create);
  }

  static MarshalerWithSize createString(List<String> values) {
    return createInternal(values, StringAnyValueMarshaler::create);
  }

  static MarshalerWithSize createBool(List<Boolean> values) {
    return createInternal(values, BoolAnyValueMarshaler::create);
  }

  static MarshalerWithSize createInt(List<Long> values) {
    return createInternal(values, IntAnyValueMarshaler::create);
  }

  static MarshalerWithSize createDouble(List<Double> values) {
    return createInternal(values, DoubleAnyValueMarshaler::create);
  }

  private static <T, M extends MarshalerWithSize> MarshalerWithSize createInternal(
      List<T> values, Function<T, M> initializer) {
    int len = values.size();
    Marshaler[] marshalers = new Marshaler[len];
    for (int i = 0; i < len; i++) {
      marshalers[i] = initializer.apply(values.get(i));
    }
    return new ArrayAnyValueMarshaler(new ArrayValueMarshaler(marshalers));
  }

  static MessageSize messageSize(
      List<io.opentelemetry.extension.incubator.logs.AnyValue<?>> value,
      MarshallingObjectsPool pool) {
    MessageSize arrayValueMessageSize = ArrayValueMarshaler.messageSize(value, pool);
    int encodedSize = MarshalerUtil.sizeMessage(AnyValue.ARRAY_VALUE, arrayValueMessageSize);

    DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
    DynamicList<MessageSize> messageFieldSizes = pool.borrowDynamicList(1);
    messageFieldSizes.add(arrayValueMessageSize);

    messageSize.set(encodedSize, messageFieldSizes);

    return messageSize;
  }

  @SuppressWarnings("unchecked")
  static void encode(
      Serializer output,
      List<io.opentelemetry.extension.incubator.logs.AnyValue<?>> value,
      MessageSize arrayAnyValueMessageSize,
      MarshallingObjectsPool pool)
      throws IOException {
    output.serializeMessage(
        AnyValue.ARRAY_VALUE,
        value,
        (Serializer serializer,
            Object messageObject,
            MessageSize messageSize,
            MarshallingObjectsPool marshallingObjectsPool) ->
            ArrayValueMarshaler.encode(
                serializer,
                (List<io.opentelemetry.extension.incubator.logs.AnyValue<?>>) messageObject,
                messageSize,
                marshallingObjectsPool),
        arrayAnyValueMessageSize.getMessageTypeFieldSize(0),
        pool);
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(AnyValue.ARRAY_VALUE, value);
  }

  private static int calculateSize(Marshaler value) {
    return MarshalerUtil.sizeMessage(AnyValue.ARRAY_VALUE, value);
  }

  private static class ArrayValueMarshaler extends MarshalerWithSize {

    private final Marshaler[] values;

    private ArrayValueMarshaler(Marshaler[] values) {
      super(calculateSize(values));
      this.values = values;
    }

    static MessageSize messageSize(
        List<io.opentelemetry.extension.incubator.logs.AnyValue<?>> values,
        MarshallingObjectsPool pool) {
      DynamicList<MessageSize> valuesSize = pool.borrowDynamicList(values.size());
      for (int i = 0; i < values.size(); i++) {
        io.opentelemetry.extension.incubator.logs.AnyValue<?> value = values.get(i);
        valuesSize.set(i, AnyValueMarshaler.messageSize(value, pool));
      }
      int encodedSize = MarshalerUtil.sizeRepeatedMessage(ArrayValue.VALUES, valuesSize);

      DefaultMessageSize messageSize = pool.getDefaultMessageSizePool().borrowObject();
      messageSize.set(encodedSize, valuesSize);
      return messageSize;
    }

    static void encode(
        Serializer output,
        List<io.opentelemetry.extension.incubator.logs.AnyValue<?>> values,
        MessageSize arrayValueMessageSize,
        MarshallingObjectsPool pool)
        throws IOException {
      output.serializeRepeatedMessage(
          ArrayValue.VALUES,
          values,
          (Serializer serializer,
              Object messageObject,
              MessageSize messageSize,
              MarshallingObjectsPool marshallingObjectsPool) ->
              AnyValueMarshaler.encode(
                  serializer,
                  (io.opentelemetry.extension.incubator.logs.AnyValue<?>) messageObject,
                  messageSize,
                  marshallingObjectsPool),
          arrayValueMessageSize.getMessageTypedFieldSizes(),
          pool);
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      output.serializeRepeatedMessage(ArrayValue.VALUES, values);
    }

    private static int calculateSize(Marshaler[] values) {
      return MarshalerUtil.sizeRepeatedMessage(ArrayValue.VALUES, values);
    }
  }
}
