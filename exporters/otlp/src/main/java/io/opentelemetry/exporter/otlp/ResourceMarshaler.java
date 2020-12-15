/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.proto.resource.v1.Resource;
import java.io.IOException;
import javax.annotation.Nullable;

final class ResourceMarshaler extends MarshalerWithSize {
  private final AttributeMarshaler[] attributeMarshalers;

  @Nullable
  static ResourceMarshaler create(io.opentelemetry.sdk.resources.Resource resource) {
    if (resource.getAttributes().isEmpty()) {
      return null;
    }
    return new ResourceMarshaler(AttributeMarshaler.createRepeated(resource.getAttributes()));
  }

  private ResourceMarshaler(AttributeMarshaler[] attributeMarshalers) {
    super(calculateSize(attributeMarshalers));
    this.attributeMarshalers = attributeMarshalers;
  }

  @Override
  public void writeTo(CodedOutputStream output) throws IOException {
    MarshalerUtil.marshalRepeatedMessage(
        Resource.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers, output);
  }

  private static int calculateSize(AttributeMarshaler[] attributeMarshalers) {
    return MarshalerUtil.sizeRepeatedMessage(Resource.ATTRIBUTES_FIELD_NUMBER, attributeMarshalers);
  }
}
