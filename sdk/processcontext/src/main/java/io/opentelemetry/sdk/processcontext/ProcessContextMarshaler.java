/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.processcontext.v1development.internal.ProcessContext;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import java.io.IOException;

final class ProcessContextMarshaler extends MarshalerWithSize {

  private final ResourceMarshaler resourceMarshaler;
  private final KeyValueMarshaler[] attributeMarshalers;

  static ProcessContextMarshaler create(ProcessContextData processContextData) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(processContextData.getAttributes());
    return new ProcessContextMarshaler(
        ResourceMarshaler.create(processContextData.getResource()), attributeMarshalers);
  }

  private ProcessContextMarshaler(
      ResourceMarshaler resourceMarshaler, KeyValueMarshaler[] attributeMarshalers) {
    super(calculateSize(resourceMarshaler, attributeMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.attributeMarshalers = attributeMarshalers;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ProcessContext.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(ProcessContext.ATTRIBUTES, attributeMarshalers);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler, KeyValueMarshaler[] attributeMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ProcessContext.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeRepeatedMessage(ProcessContext.ATTRIBUTES, attributeMarshalers);
    return size;
  }
}
