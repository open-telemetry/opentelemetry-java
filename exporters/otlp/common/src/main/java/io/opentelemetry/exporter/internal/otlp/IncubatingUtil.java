/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.incubator.internal.InternalExtendedAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.logs.v1.internal.LogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class IncubatingUtil {

  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.common.ExtendedAttributes");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  private static final byte[] EMPTY_BYTES = new byte[0];
  private static final KeyValueMarshaler[] EMPTY_REPEATED = new KeyValueMarshaler[0];

  private IncubatingUtil() {}

  public static boolean isExtendedLogRecordData(LogRecordData logRecordData) {
    return INCUBATOR_AVAILABLE && logRecordData instanceof ExtendedLogRecordData;
  }

  @SuppressWarnings("AvoidObjectArrays")
  public static KeyValueMarshaler[] createdExtendedAttributesMarhsalers(
      LogRecordData logRecordData) {
    return createForExtendedAttributes(getExtendedAttributes(logRecordData));
  }

  public static int extendedAttributesSize(LogRecordData logRecordData) {
    return getExtendedAttributes(logRecordData).size();
  }

  // TODO(jack-berg): move to KeyValueMarshaler when ExtendedAttributes is stable
  private static KeyValueMarshaler[] createForExtendedAttributes(ExtendedAttributes attributes) {
    if (attributes.isEmpty()) {
      return EMPTY_REPEATED;
    }

    KeyValueMarshaler[] marshalers = new KeyValueMarshaler[attributes.size()];
    attributes.forEach(
        new BiConsumer<ExtendedAttributeKey<?>, Object>() {
          int index = 0;

          @Override
          public void accept(ExtendedAttributeKey<?> attributeKey, Object o) {
            marshalers[index++] = create(attributeKey, o);
          }
        });
    return marshalers;
  }

  // TODO(jack-berg): move to KeyValueMarshaler when ExtendedAttributes is stable
  // Supporting deprecated EXTENDED_ATTRIBUTES type until removed
  @SuppressWarnings({"unchecked", "deprecation"})
  private static KeyValueMarshaler create(ExtendedAttributeKey<?> attributeKey, Object value) {
    byte[] keyUtf8;
    if (attributeKey.getKey().isEmpty()) {
      keyUtf8 = EMPTY_BYTES;
    } else if (attributeKey instanceof InternalExtendedAttributeKeyImpl) {
      keyUtf8 = ((InternalExtendedAttributeKeyImpl<?>) attributeKey).getKeyUtf8();
    } else {
      keyUtf8 = attributeKey.getKey().getBytes(StandardCharsets.UTF_8);
    }
    switch (attributeKey.getType()) {
      case STRING:
        return new KeyValueMarshaler(keyUtf8, StringAnyValueMarshaler.create((String) value));
      case LONG:
        return new KeyValueMarshaler(keyUtf8, IntAnyValueMarshaler.create((long) value));
      case BOOLEAN:
        return new KeyValueMarshaler(keyUtf8, BoolAnyValueMarshaler.create((boolean) value));
      case DOUBLE:
        return new KeyValueMarshaler(keyUtf8, DoubleAnyValueMarshaler.create((double) value));
      case STRING_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createString((List<String>) value));
      case LONG_ARRAY:
        return new KeyValueMarshaler(keyUtf8, ArrayAnyValueMarshaler.createInt((List<Long>) value));
      case BOOLEAN_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createBool((List<Boolean>) value));
      case DOUBLE_ARRAY:
        return new KeyValueMarshaler(
            keyUtf8, ArrayAnyValueMarshaler.createDouble((List<Double>) value));
      case EXTENDED_ATTRIBUTES:
        return new KeyValueMarshaler(
            keyUtf8,
            new KeyValueListAnyValueMarshaler(
                new KeyValueListAnyValueMarshaler.KeyValueListMarshaler(
                    createForExtendedAttributes((ExtendedAttributes) value))));
      case VALUE:
        return new KeyValueMarshaler(keyUtf8, AnyValueMarshaler.create((Value<?>) value));
    }
    // Error prone ensures the switch statement is complete, otherwise only can happen with
    // unaligned versions which are not supported.
    throw new IllegalArgumentException("Unsupported attribute type.");
  }

  public static int sizeExtendedAttributes(LogRecordData log, MarshalerContext context) {
    return ExtendedAttributeKeyValueStatelessMarshaler.sizeExtendedAttributes(
        LogRecord.ATTRIBUTES, getExtendedAttributes(log), context);
  }

  public static void serializeExtendedAttributes(
      Serializer output, LogRecordData log, MarshalerContext context) throws IOException {
    ExtendedAttributeKeyValueStatelessMarshaler.serializeExtendedAttributes(
        output, LogRecord.ATTRIBUTES, getExtendedAttributes(log), context);
  }

  private static ExtendedAttributes getExtendedAttributes(LogRecordData logRecordData) {
    if (!(logRecordData instanceof ExtendedLogRecordData)) {
      throw new IllegalArgumentException("logRecordData must be ExtendedLogRecordData");
    }
    return ((ExtendedLogRecordData) logRecordData).getExtendedAttributes();
  }
}
