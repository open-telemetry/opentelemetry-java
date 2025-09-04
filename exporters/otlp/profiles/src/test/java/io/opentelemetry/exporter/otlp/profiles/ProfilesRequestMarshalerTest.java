/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableFunctionData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableKeyValueAndUnitData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLineData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLinkData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLocationData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableMappingData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileDictionaryData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableSampleData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableStackData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableValueTypeData;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.profiles.v1development.Function;
import io.opentelemetry.proto.profiles.v1development.KeyValueAndUnit;
import io.opentelemetry.proto.profiles.v1development.Line;
import io.opentelemetry.proto.profiles.v1development.Link;
import io.opentelemetry.proto.profiles.v1development.Location;
import io.opentelemetry.proto.profiles.v1development.Mapping;
import io.opentelemetry.proto.profiles.v1development.Profile;
import io.opentelemetry.proto.profiles.v1development.ResourceProfiles;
import io.opentelemetry.proto.profiles.v1development.Sample;
import io.opentelemetry.proto.profiles.v1development.ScopeProfiles;
import io.opentelemetry.proto.profiles.v1development.Stack;
import io.opentelemetry.proto.profiles.v1development.ValueType;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ProfilesRequestMarshalerTest {

  @Test
  void compareFunctionMarshaling() {
    FunctionData input = ImmutableFunctionData.create(1, 2, 3, 4);
    Function builderResult =
        Function.newBuilder()
            .setNameStrindex(1)
            .setSystemNameStrindex(2)
            .setFilenameStrindex(3)
            .setStartLine(4)
            .build();

    Function roundTripResult =
        parse(Function.getDefaultInstance(), FunctionMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedFunctionMarshaling() {
    List<FunctionData> inputs = new ArrayList<>();
    inputs.add(ImmutableFunctionData.create(1, 2, 3, 4));
    inputs.add(ImmutableFunctionData.create(5, 6, 7, 8));

    List<Function> builderResults = new ArrayList<>();
    builderResults.add(
        Function.newBuilder()
            .setNameStrindex(1)
            .setSystemNameStrindex(2)
            .setFilenameStrindex(3)
            .setStartLine(4)
            .build());
    builderResults.add(
        Function.newBuilder()
            .setNameStrindex(5)
            .setSystemNameStrindex(6)
            .setFilenameStrindex(7)
            .setStartLine(8)
            .build());

    FunctionMarshaler[] marshalers = FunctionMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Function roundTripResult = parse(Function.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareLineMarshaling() {
    LineData input = ImmutableLineData.create(1, 2, 3);
    Line builderResult = Line.newBuilder().setFunctionIndex(1).setLine(2).setColumn(3).build();

    Line roundTripResult = parse(Line.getDefaultInstance(), LineMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedLineMarshaling() {
    List<LineData> inputs = new ArrayList<>();
    inputs.add(ImmutableLineData.create(1, 2, 3));
    inputs.add(ImmutableLineData.create(4, 5, 6));

    List<Line> builderResults = new ArrayList<>();
    builderResults.add(Line.newBuilder().setFunctionIndex(1).setLine(2).setColumn(3).build());
    builderResults.add(Line.newBuilder().setFunctionIndex(4).setLine(5).setColumn(6).build());

    LineMarshaler[] marshalers = LineMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Line roundTripResult = parse(Line.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareKeyValueAndUnitMarshaling() {
    KeyValueAndUnitData input = ImmutableKeyValueAndUnitData.create(1, Value.of("foo"), 3);
    KeyValueAndUnit builderResult =
        KeyValueAndUnit.newBuilder()
            .setKeyStrindex(1)
            .setValue(AnyValue.newBuilder().setStringValue("foo").build())
            .setUnitStrindex(3)
            .build();

    KeyValueAndUnit roundTripResult =
        parse(KeyValueAndUnit.getDefaultInstance(), KeyValueAndUnitMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedKeyValueAndUnitMarshaling() {
    List<KeyValueAndUnitData> inputs = new ArrayList<>();
    inputs.add(ImmutableKeyValueAndUnitData.create(1, Value.of("foo"), 3));
    inputs.add(ImmutableKeyValueAndUnitData.create(4, Value.of("bar"), 6));

    List<KeyValueAndUnit> builderResults = new ArrayList<>();
    builderResults.add(
        KeyValueAndUnit.newBuilder()
            .setKeyStrindex(1)
            .setValue(AnyValue.newBuilder().setStringValue("foo").build())
            .setUnitStrindex(3)
            .build());
    builderResults.add(
        KeyValueAndUnit.newBuilder()
            .setKeyStrindex(4)
            .setValue(AnyValue.newBuilder().setStringValue("bar").build())
            .setUnitStrindex(6)
            .build());

    KeyValueAndUnitMarshaler[] marshalers = KeyValueAndUnitMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      KeyValueAndUnit roundTripResult = parse(KeyValueAndUnit.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareLinkMarshaling() {
    String traceId = "0123456789abcdef0123456789abcdef";
    String spanId = "fedcba9876543210";
    LinkData input = ImmutableLinkData.create(traceId, spanId);
    Link builderResult =
        Link.newBuilder()
            .setTraceId(ByteString.fromHex(traceId))
            .setSpanId(ByteString.fromHex(spanId))
            .build();

    Link roundTripResult = parse(Link.getDefaultInstance(), LinkMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedLinkMarshaling() {
    List<LinkData> inputs = new ArrayList<>();
    inputs.add(ImmutableLinkData.create("0123456789abcdef0123456789abcdef", "fedcba9876543210"));
    inputs.add(ImmutableLinkData.create("123456789abcdef0123456789abcdef0", "edcba9876543210f"));

    List<Link> builderResults = new ArrayList<>();
    builderResults.add(
        Link.newBuilder()
            .setTraceId(ByteString.fromHex("0123456789abcdef0123456789abcdef"))
            .setSpanId(ByteString.fromHex("fedcba9876543210"))
            .build());
    builderResults.add(
        Link.newBuilder()
            .setTraceId(ByteString.fromHex("123456789abcdef0123456789abcdef0"))
            .setSpanId(ByteString.fromHex("edcba9876543210f"))
            .build());

    LinkMarshaler[] marshalers = LinkMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Link roundTripResult = parse(Link.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareLocationMarshaling() {
    LocationData input = ImmutableLocationData.create(1, 2, Collections.emptyList(), listOf(4, 5));
    Location builderResult =
        Location.newBuilder()
            .setMappingIndex(1)
            .setAddress(2)
            .addAllAttributeIndices(listOf(4, 5))
            .build();

    Location roundTripResult =
        parse(Location.getDefaultInstance(), LocationMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedLocationMarshaling() {
    List<LocationData> inputs = new ArrayList<>();
    inputs.add(ImmutableLocationData.create(1, 2, Collections.emptyList(), listOf(3, 4)));
    inputs.add(ImmutableLocationData.create(5, 6, Collections.emptyList(), listOf(7, 8)));

    List<Location> builderResults = new ArrayList<>();
    builderResults.add(
        Location.newBuilder()
            .setMappingIndex(1)
            .setAddress(2)
            .addAllAttributeIndices(listOf(3, 4))
            .build());
    builderResults.add(
        Location.newBuilder()
            .setMappingIndex(5)
            .setAddress(6)
            .addAllAttributeIndices(listOf(7, 8))
            .build());

    LocationMarshaler[] marshalers = LocationMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Location roundTripResult = parse(Location.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareMappingMarshaling() {
    MappingData input = ImmutableMappingData.create(1, 2, 3, 4, listOf(5, 6));
    Mapping builderResult =
        Mapping.newBuilder()
            .setMemoryStart(1)
            .setMemoryLimit(2)
            .setFileOffset(3)
            .setFilenameStrindex(4)
            .addAllAttributeIndices(listOf(5, 6))
            .build();

    Mapping roundTripResult = parse(Mapping.getDefaultInstance(), MappingMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedMappingMarshaling() {
    List<MappingData> inputs = new ArrayList<>();
    inputs.add(ImmutableMappingData.create(1, 2, 3, 4, listOf(5, 6)));
    inputs.add(ImmutableMappingData.create(7, 8, 9, 10, listOf(11, 12)));

    List<Mapping> builderResults = new ArrayList<>();
    builderResults.add(
        Mapping.newBuilder()
            .setMemoryStart(1)
            .setMemoryLimit(2)
            .setFileOffset(3)
            .setFilenameStrindex(4)
            .addAllAttributeIndices(listOf(5, 6))
            .build());
    builderResults.add(
        Mapping.newBuilder()
            .setMemoryStart(7)
            .setMemoryLimit(8)
            .setFileOffset(9)
            .setFilenameStrindex(10)
            .addAllAttributeIndices(listOf(11, 12))
            .build());

    MappingMarshaler[] marshalers = MappingMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Mapping roundTripResult = parse(Mapping.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareStackMarshaling() {
    StackData input = ImmutableStackData.create(listOf(1, 2));
    Stack builderResult = Stack.newBuilder().addAllLocationIndices(listOf(1, 2)).build();

    Stack roundTripResult = parse(Stack.getDefaultInstance(), StackMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedStackMarshaling() {
    List<StackData> inputs = new ArrayList<>();
    inputs.add(ImmutableStackData.create(listOf(1, 2)));
    inputs.add(ImmutableStackData.create(listOf(3, 4)));

    List<Stack> builderResults = new ArrayList<>();
    builderResults.add(Stack.newBuilder().addAllLocationIndices(listOf(1, 2)).build());
    builderResults.add(Stack.newBuilder().addAllLocationIndices(listOf(3, 4)).build());

    StackMarshaler[] marshalers = StackMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Stack roundTripResult = parse(Stack.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareResourceProfilesMarshaling() {

    String profileId = "0123456789abcdef0123456789abcdef";
    ProfileData profileContainerData =
        ImmutableProfileData.create(
            Resource.create(Attributes.empty()),
            InstrumentationScopeInfo.create("testscope"),
            ImmutableProfileDictionaryData.create(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
            Collections.emptyList(),
            5L,
            6L,
            ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
            7L,
            listOf(8, 9),
            profileId,
            Collections.emptyList(),
            3,
            "format",
            ByteBuffer.wrap(new byte[] {4, 5}));

    Collection<ProfileData> input = new ArrayList<>();
    input.add(profileContainerData);

    Profile profileContainer =
        Profile.newBuilder()
            .setSampleType(
                ValueType.newBuilder()
                    .setTypeStrindex(1)
                    .setUnitStrindex(2)
                    .setAggregationTemporality(
                        io.opentelemetry.proto.profiles.v1development.AggregationTemporality
                            .AGGREGATION_TEMPORALITY_CUMULATIVE)
                    .build())
            .setProfileId(ByteString.fromHex(profileId))
            .setDroppedAttributesCount(3)
            .setOriginalPayloadFormat("format")
            .setOriginalPayload(ByteString.copyFrom(new byte[] {4, 5}))
            .setTimeUnixNano(5)
            .setDurationNano(6)
            .setPeriod(7)
            .setPeriodType(
                ValueType.newBuilder()
                    .setTypeStrindex(1)
                    .setUnitStrindex(2)
                    .setAggregationTemporality(
                        io.opentelemetry.proto.profiles.v1development.AggregationTemporality
                            .AGGREGATION_TEMPORALITY_CUMULATIVE)
                    .build())
            .addAllCommentStrindices(listOf(8, 9))
            .build();

    ResourceProfiles builderResult =
        ResourceProfiles.newBuilder()
            .setResource(io.opentelemetry.proto.resource.v1.Resource.newBuilder().build())
            .addScopeProfiles(
                ScopeProfiles.newBuilder()
                    .setScope(InstrumentationScope.newBuilder().setName("testscope").build())
                    .addProfiles(profileContainer)
                    .build())
            .build();

    ResourceProfilesMarshaler[] marshalers = ResourceProfilesMarshaler.create(input);
    assertThat(marshalers.length).isEqualTo(1);
    ResourceProfiles roundTripResult = parse(ResourceProfiles.getDefaultInstance(), marshalers[0]);
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareSampleMarshaling() {
    SampleData input =
        ImmutableSampleData.create(1, listOf(3L, 4L), listOf(5, 6), 7, listOf(8L, 9L));
    Sample builderResult =
        Sample.newBuilder()
            .setStackIndex(1)
            .addAllValues(listOf(3L, 4L))
            .addAllAttributeIndices(listOf(5, 6))
            .setLinkIndex(7)
            .addAllTimestampsUnixNano(listOf(8L, 9L))
            .build();

    Sample roundTripResult = parse(Sample.getDefaultInstance(), SampleMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedSampleMarshaling() {
    List<SampleData> inputs = new ArrayList<>();
    inputs.add(ImmutableSampleData.create(1, listOf(3L, 4L), listOf(5, 6), 7, listOf(8L, 9L)));
    inputs.add(
        ImmutableSampleData.create(10, listOf(12L, 13L), listOf(14, 15), 16, listOf(17L, 18L)));

    List<Sample> builderResults = new ArrayList<>();
    builderResults.add(
        Sample.newBuilder()
            .setStackIndex(1)
            .addAllValues(listOf(3L, 4L))
            .addAllAttributeIndices(listOf(5, 6))
            .setLinkIndex(7)
            .addAllTimestampsUnixNano(listOf(8L, 9L))
            .build());
    builderResults.add(
        Sample.newBuilder()
            .setStackIndex(10)
            .addAllValues(listOf(12L, 13L))
            .addAllAttributeIndices(listOf(14, 15))
            .setLinkIndex(16)
            .addAllTimestampsUnixNano(listOf(17L, 18L))
            .build());

    SampleMarshaler[] marshalers = SampleMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      Sample roundTripResult = parse(Sample.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  @Test
  void compareValueTypeMarshaling() {
    ValueTypeData input = ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE);
    ValueType builderResult =
        ValueType.newBuilder()
            .setTypeStrindex(1)
            .setUnitStrindex(2)
            .setAggregationTemporality(
                io.opentelemetry.proto.profiles.v1development.AggregationTemporality
                    .AGGREGATION_TEMPORALITY_CUMULATIVE)
            .build();

    ValueType roundTripResult =
        parse(ValueType.getDefaultInstance(), ValueTypeMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareRepeatedValueTypeMarshaling() {
    List<ValueTypeData> inputs = new ArrayList<>();
    inputs.add(ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE));
    inputs.add(ImmutableValueTypeData.create(3, 4, AggregationTemporality.CUMULATIVE));

    List<ValueType> builderResults = new ArrayList<>();
    builderResults.add(
        ValueType.newBuilder()
            .setTypeStrindex(1)
            .setUnitStrindex(2)
            .setAggregationTemporality(
                io.opentelemetry.proto.profiles.v1development.AggregationTemporality
                    .AGGREGATION_TEMPORALITY_CUMULATIVE)
            .build());
    builderResults.add(
        ValueType.newBuilder()
            .setTypeStrindex(3)
            .setUnitStrindex(4)
            .setAggregationTemporality(
                io.opentelemetry.proto.profiles.v1development.AggregationTemporality
                    .AGGREGATION_TEMPORALITY_CUMULATIVE)
            .build());

    ValueTypeMarshaler[] marshalers = ValueTypeMarshaler.createRepeated(inputs);

    for (int i = 0; i < marshalers.length; i++) {
      ValueType roundTripResult = parse(ValueType.getDefaultInstance(), marshalers[i]);
      assertThat(roundTripResult).isEqualTo(builderResults.get(i));
    }
  }

  private static <T> List<T> listOf(T a, T b) {
    ArrayList<T> list = new ArrayList<>();
    list.add(a);
    list.add(b);
    return Collections.unmodifiableList(list);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // We don't compare JSON strings due to some differences (particularly serializing enums as
    // numbers instead of names). This may improve in the future but what matters is what we produce
    // can be parsed.
    String json = toJson(marshaler);
    Message.Builder builder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    assertThat(builder.build()).isEqualTo(result);

    return result;
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }

  private static String toJson(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }
}
