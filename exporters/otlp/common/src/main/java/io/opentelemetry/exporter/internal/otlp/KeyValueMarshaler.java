/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.extension.incubator.logs.KeyAnyValue;
import io.opentelemetry.proto.common.v1.internal.KeyValue;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A Marshaler of key value pairs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class KeyValueMarshaler extends Marshaler {

  abstract String getKey();
  abstract Marshaler getValue();

  /** Returns Marshaler for the given KeyAnyValue. */

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(KeyValue.KEY, getKey());
    output.serializeMessage(KeyValue.VALUE, getValue());
  }

  protected static int calculateSize(String key, Marshaler value) {
    int size = 0;
    size += MarshalerUtil.sizeStringUtf8(KeyValue.KEY, key);
    size += MarshalerUtil.sizeMessage(KeyValue.VALUE, value);
    return size;
  }
}
