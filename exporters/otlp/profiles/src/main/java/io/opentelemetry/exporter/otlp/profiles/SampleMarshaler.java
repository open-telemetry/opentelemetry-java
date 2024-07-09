/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1experimental.internal.Sample;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class SampleMarshaler extends MarshalerWithSize {

  private static final SampleMarshaler[] EMPTY_REPEATED = new SampleMarshaler[0];

  private final long locationsStartIndex;
  private final long locationsLength;
  private final int stacktraceIdIndex;
  private final List<Long> values;
  private final List<Long> attributes;
  private final long link;
  private final List<Long> timestamps;

  static SampleMarshaler create(SampleData sampleData) {

    return new SampleMarshaler(
        sampleData.getLocationsStartIndex(),
        sampleData.getLocationsLength(),
        sampleData.getStacktraceIdIndex(),
        sampleData.getValues(),
        sampleData.getAttributes(),
        sampleData.getLink(),
        sampleData.getTimestamps());
  }

  static SampleMarshaler[] createRepeated(List<SampleData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    SampleMarshaler[] sampleMarshalers = new SampleMarshaler[items.size()];
    items.forEach(
        item ->
            new Consumer<SampleData>() {
              int index = 0;

              @Override
              public void accept(SampleData sampleData) {
                sampleMarshalers[index++] = SampleMarshaler.create(sampleData);
              }
            });
    return sampleMarshalers;
  }

  private SampleMarshaler(
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      List<Long> values,
      List<Long> attributes,
      long link,
      List<Long> timestamps) {
    super(
        calculateSize(
            locationsStartIndex,
            locationsLength,
            stacktraceIdIndex,
            values,
            attributes,
            link,
            timestamps));
    this.locationsStartIndex = locationsStartIndex;
    this.locationsLength = locationsLength;
    this.stacktraceIdIndex = stacktraceIdIndex;
    this.values = values;
    this.attributes = attributes;
    this.link = link;
    this.timestamps = timestamps;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeUInt64(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    output.serializeUInt64(Sample.LOCATIONS_LENGTH, locationsLength);
    output.serializeUInt32(Sample.STACKTRACE_ID_INDEX, stacktraceIdIndex);
    output.serializeRepeatedInt64(Sample.VALUE, values);
    output.serializeRepeatedUInt64(Sample.ATTRIBUTES, attributes);
    output.serializeUInt64(Sample.LINK, link);
    output.serializeRepeatedUInt64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
  }

  private static int calculateSize(
      long locationsStartIndex,
      long locationsLength,
      int stacktraceIdIndex,
      List<Long> values,
      List<Long> attributes,
      long link,
      List<Long> timestamps) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeUInt64(Sample.LOCATIONS_START_INDEX, locationsStartIndex);
    size += MarshalerUtil.sizeUInt64(Sample.LOCATIONS_LENGTH, locationsLength);
    size += MarshalerUtil.sizeUInt32(Sample.STACKTRACE_ID_INDEX, stacktraceIdIndex);
    size += MarshalerUtil.sizeRepeatedInt64(Sample.VALUE, values);
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.ATTRIBUTES, attributes);
    size += MarshalerUtil.sizeUInt64(Sample.LINK, link);
    size += MarshalerUtil.sizeRepeatedUInt64(Sample.TIMESTAMPS_UNIX_NANO, timestamps);
    return size;
  }
}
