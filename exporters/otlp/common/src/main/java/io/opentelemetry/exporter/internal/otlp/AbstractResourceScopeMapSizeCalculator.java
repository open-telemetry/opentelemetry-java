/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.CodedOutputStream;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractResourceScopeMapSizeCalculator<T>
    implements BiConsumer<Resource, Map<InstrumentationScopeInfo, List<T>>> {
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
      Resource resource, Map<InstrumentationScopeInfo, List<T>> instrumentationScopeInfoListMap);

  @Override
  public void accept(
      Resource resource, Map<InstrumentationScopeInfo, List<T>> instrumentationScopeInfoListMap) {
    int fieldSize = calculateSize(resource, instrumentationScopeInfoListMap);
    size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
  }

  public int getSize() {
    return size;
  }
}
