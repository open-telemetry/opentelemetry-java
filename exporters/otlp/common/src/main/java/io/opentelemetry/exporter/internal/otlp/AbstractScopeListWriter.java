/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractScopeListWriter<T>
    implements BiConsumer<InstrumentationScopeInfo, List<T>> {
  @SuppressWarnings("NullAway")
  protected Serializer output;

  @SuppressWarnings("NullAway")
  protected ProtoFieldInfo field;

  @SuppressWarnings("NullAway")
  protected MarshalerContext context;

  public void initialize(Serializer output, ProtoFieldInfo field, MarshalerContext context) {
    this.output = output;
    this.field = field;
    this.context = context;
  }

  protected abstract void handle(
      InstrumentationScopeMarshaler instrumentationScopeMarshaler,
      List<T> list,
      byte[] schemaUrlUtf8)
      throws IOException;

  @Override
  public void accept(InstrumentationScopeInfo instrumentationScopeInfo, List<T> list) {
    try {
      output.writeStartRepeated(field);
      output.writeStartRepeatedElement(field, context.getSize());

      InstrumentationScopeMarshaler instrumentationScopeMarshaler =
          context.getObject(InstrumentationScopeMarshaler.class);
      byte[] schemaUrlUtf8 = context.getByteArray();
      handle(instrumentationScopeMarshaler, list, schemaUrlUtf8);

      output.writeEndRepeatedElement();
      output.writeEndRepeated();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
