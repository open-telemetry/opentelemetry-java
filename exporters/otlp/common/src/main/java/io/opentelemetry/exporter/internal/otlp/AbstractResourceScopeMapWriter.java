/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractResourceScopeMapWriter<T>
    implements BiConsumer<Resource, Map<InstrumentationScopeInfo, List<T>>> {
  @SuppressWarnings("NullAway")
  protected Serializer output;
  @SuppressWarnings("NullAway")
  private ProtoFieldInfo field;
  @SuppressWarnings("NullAway")
  protected MarshalerContext context;

  public void initialize(Serializer output, ProtoFieldInfo field, MarshalerContext context) {
    this.output = output;
    this.field = field;
    this.context = context;
  }

  protected abstract void handle(
      Map<InstrumentationScopeInfo, List<T>> instrumentationScopeInfoListMap) throws IOException;

  @Override
  public void accept(
      Resource resource, Map<InstrumentationScopeInfo, List<T>> instrumentationScopeInfoListMap) {
    try {
      output.writeStartRepeated(field);
      output.writeStartRepeatedElement(field, context.getSize());

      handle(instrumentationScopeInfoListMap);

      output.writeEndRepeatedElement();
      output.writeEndRepeated();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
