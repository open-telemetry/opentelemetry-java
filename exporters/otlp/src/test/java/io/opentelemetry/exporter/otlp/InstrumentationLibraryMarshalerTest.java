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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstrumentationLibraryMarshalerTest {
  @Test
  void customMarshalAndSize() throws IOException {
    List<InstrumentationLibraryInfo> instrumentationLibraryInfos =
        Arrays.asList(
            InstrumentationLibraryInfo.create("name", null),
            InstrumentationLibraryInfo.create("name", ""),
            InstrumentationLibraryInfo.create("name", "version"));

    for (InstrumentationLibraryInfo instrumentationLibraryInfo : instrumentationLibraryInfos) {
      InstrumentationLibrary proto =
          CommonAdapter.toProtoInstrumentationLibrary(instrumentationLibraryInfo);
      int protoSize = proto.getSerializedSize();

      InstrumentationLibraryMarshaler marshaler =
          InstrumentationLibraryMarshaler.create(instrumentationLibraryInfo);
      assertThat(marshaler).isNotNull();
      int customSize = marshaler.getSerializedSize();
      assertThat(customSize).isEqualTo(protoSize);

      ByteBuffer protoOutput = ByteBuffer.allocate(protoSize);
      proto.writeTo(CodedOutputStream.newInstance(protoOutput));

      ByteBuffer customOutput = ByteBuffer.allocate(customSize);
      marshaler.writeTo(CodedOutputStream.newInstance(customOutput));

      assertThat(customOutput).isEqualTo(protoOutput);
    }
  }

  @Test
  void customMarshalAndSize_Empty() {
    assertThat(InstrumentationLibraryMarshaler.create(InstrumentationLibraryInfo.getEmpty()))
        .isNull();
    assertThat(InstrumentationLibraryMarshaler.create(InstrumentationLibraryInfo.create("", "")))
        .isNull();
  }
}
