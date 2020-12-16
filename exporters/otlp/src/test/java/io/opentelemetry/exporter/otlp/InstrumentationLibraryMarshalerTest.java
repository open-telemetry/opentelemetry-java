/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class InstrumentationLibraryMarshalerTest {
  @Test
  void customMarshalAndSize() throws IOException {
    assertMarshalAndSize(InstrumentationLibraryInfo.create("name", null));
    assertMarshalAndSize(InstrumentationLibraryInfo.create("name", ""));
    assertMarshalAndSize(InstrumentationLibraryInfo.create("name", "version"));
  }

  @Test
  void customMarshalAndSize_Empty() throws IOException {
    assertMarshalAndSize(InstrumentationLibraryInfo.getEmpty());
    assertMarshalAndSize(InstrumentationLibraryInfo.create("", ""));
  }

  private static void assertMarshalAndSize(InstrumentationLibraryInfo instrumentationLibraryInfo)
      throws IOException {
    InstrumentationLibrary proto =
        CommonAdapter.toProtoInstrumentationLibrary(instrumentationLibraryInfo);
    InstrumentationLibraryMarshaler marshaler =
        InstrumentationLibraryMarshaler.create(instrumentationLibraryInfo);
    assertThat(marshaler.getSerializedSize()).isEqualTo(proto.getSerializedSize());

    byte[] protoOutput = new byte[proto.getSerializedSize()];
    proto.writeTo(CodedOutputStream.newInstance(protoOutput));

    byte[] customOutput = new byte[marshaler.getSerializedSize()];
    marshaler.writeTo(CodedOutputStream.newInstance(customOutput));
    assertThat(customOutput).isEqualTo(protoOutput);
  }
}
