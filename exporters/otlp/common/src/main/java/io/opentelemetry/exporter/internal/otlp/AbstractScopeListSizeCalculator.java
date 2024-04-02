/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class AbstractScopeListSizeCalculator<T>
    implements BiConsumer<InstrumentationScopeInfo, List<T>> {
  private int size;
  private int fieldTagSize;
  @SuppressWarnings("NullAway")
  protected MarshalerContext context;

  public void initialize(ProtoFieldInfo field, MarshalerContext context) {
    this.size = 0;
    this.fieldTagSize = field.getTagSize();
    this.context = context;
  }

  public abstract int calculateSize(
      InstrumentationScopeMarshaler instrumentationScopeMarshaler,
      byte[] schemaUrlUtf8,
      List<T> list);

  @Override
  public void accept(InstrumentationScopeInfo instrumentationScopeInfo, List<T> list) {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        InstrumentationScopeMarshaler.create(instrumentationScopeInfo);
    context.addData(instrumentationScopeMarshaler);
    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(instrumentationScopeInfo.getSchemaUrl());
    context.addData(schemaUrlUtf8);
    int fieldSize = calculateSize(instrumentationScopeMarshaler, schemaUrlUtf8, list);
    size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
  }

  public int getSize() {
    return size;
  }
}
