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
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableAttributeUnitData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableFunctionData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLineData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLinkData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLocationData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableMappingData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileContainerData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableProfileData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableSampleData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableValueTypeData;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.profiles.v1experimental.AttributeUnit;
import io.opentelemetry.proto.profiles.v1experimental.Function;
import io.opentelemetry.proto.profiles.v1experimental.Line;
import io.opentelemetry.proto.profiles.v1experimental.Link;
import io.opentelemetry.proto.profiles.v1experimental.Location;
import io.opentelemetry.proto.profiles.v1experimental.Mapping;
import io.opentelemetry.proto.profiles.v1experimental.Profile;
import io.opentelemetry.proto.profiles.v1experimental.ProfileContainer;
import io.opentelemetry.proto.profiles.v1experimental.ResourceProfiles;
import io.opentelemetry.proto.profiles.v1experimental.Sample;
import io.opentelemetry.proto.profiles.v1experimental.ScopeProfiles;
import io.opentelemetry.proto.profiles.v1experimental.ValueType;
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
  void compareAttributeUnitMarshaling() {
    AttributeUnitData input = ImmutableAttributeUnitData.create(1, 2);
    AttributeUnit builderResult = AttributeUnit.newBuilder().setAttributeKey(1).setUnit(2).build();

    AttributeUnit roundTripResult =
        parse(AttributeUnit.getDefaultInstance(), AttributeUnitMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareFunctionMarshaling() {
    FunctionData input = ImmutableFunctionData.create(1, 2, 3, 4);
    Function builderResult =
        Function.newBuilder().setName(1).setSystemName(2).setFilename(3).setStartLine(4).build();

    Function roundTripResult =
        parse(Function.getDefaultInstance(), FunctionMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareLineMarshaling() {
    LineData input = ImmutableLineData.create(1, 2, 3);
    Line builderResult = Line.newBuilder().setFunctionIndex(1).setLine(2).setColumn(3).build();

    Line roundTripResult = parse(Line.getDefaultInstance(), LineMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
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
  void compareLocationMarshaling() {
    LocationData input =
        ImmutableLocationData.create(1, 2, Collections.emptyList(), true, 3, listOf(5L, 6L));
    Location builderResult =
        Location.newBuilder()
            .setMappingIndex(1)
            .setAddress(2)
            .setIsFolded(true)
            .setTypeIndex(3)
            .addAllAttributes(listOf(5L, 6L))
            .build();

    Location roundTripResult =
        parse(Location.getDefaultInstance(), LocationMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareMappingMarshaling() {
    MappingData input =
        ImmutableMappingData.create(
            1, 2, 3, 4, 5, BuildIdKind.LINKER, listOf(6L, 7L), true, true, true, true);
    Mapping builderResult =
        Mapping.newBuilder()
            .setMemoryStart(1)
            .setMemoryLimit(2)
            .setFileOffset(3)
            .setFilename(4)
            .setBuildId(5)
            .setBuildIdKind(
                io.opentelemetry.proto.profiles.v1experimental.BuildIdKind.BUILD_ID_LINKER)
            .addAllAttributes(listOf(6L, 7L))
            .setHasFunctions(true)
            .setHasFilenames(true)
            .setHasLineNumbers(true)
            .setHasInlineFrames(true)
            .build();

    Mapping roundTripResult = parse(Mapping.getDefaultInstance(), MappingMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareProfileContainerMarshaling() {
    String profileId = "0123456789abcdef0123456789abcdef";
    ProfileContainerData input =
        ImmutableProfileContainerData.create(
            Resource.getDefault(),
            InstrumentationScopeInfo.empty(),
            profileId,
            1,
            2,
            Attributes.empty(),
            3,
            "format",
            ByteBuffer.wrap(new byte[] {4, 5}),
            sampleProfileData());

    ProfileContainer builderResult =
        ProfileContainer.newBuilder()
            .setProfileId(ByteString.fromHex(profileId))
            .setStartTimeUnixNano(1)
            .setEndTimeUnixNano(2)
            .setDroppedAttributesCount(3)
            .setOriginalPayloadFormat("format")
            .setOriginalPayload(ByteString.copyFrom(new byte[] {4, 5}))
            .setProfile(sampleProfileBuilder().build())
            .build();

    ProfileContainer roundTripResult =
        parse(ProfileContainer.getDefaultInstance(), ProfileContainerMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareResourceProfilesMarshaling() {

    String profileId = "0123456789abcdef0123456789abcdef";
    ProfileContainerData profileContainerData =
        ImmutableProfileContainerData.create(
            Resource.create(Attributes.empty()),
            InstrumentationScopeInfo.create("testscope"),
            profileId,
            1,
            2,
            Attributes.empty(),
            3,
            "format",
            ByteBuffer.wrap(new byte[] {4, 5}),
            sampleProfileData());

    Collection<ProfileContainerData> input = new ArrayList<>();
    input.add(profileContainerData);

    ProfileContainer profileContainer =
        ProfileContainer.newBuilder()
            .setProfileId(ByteString.fromHex(profileId))
            .setStartTimeUnixNano(1)
            .setEndTimeUnixNano(2)
            .setDroppedAttributesCount(3)
            .setOriginalPayloadFormat("format")
            .setOriginalPayload(ByteString.copyFrom(new byte[] {4, 5}))
            .setProfile(sampleProfileBuilder().build())
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
    // TODO
  }

  @Test
  void compareProfileMarshaling() {
    ProfileData input = sampleProfileData();
    Profile builderResult = sampleProfileBuilder().build();
    Profile roundTripResult = parse(Profile.getDefaultInstance(), ProfileMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareSampleMarshaling() {
    SampleData input =
        ImmutableSampleData.create(1, 2, 3, listOf(4L, 5L), listOf(6L, 7L), 8L, listOf(9L, 10L));
    Sample builderResult =
        Sample.newBuilder()
            .setLocationsStartIndex(1)
            .setLocationsLength(2)
            .setStacktraceIdIndex(3)
            .addAllValue(listOf(4L, 5L))
            .addAllAttributes(listOf(6L, 7L))
            .setLink(8)
            .addAllTimestampsUnixNano(listOf(9L, 10L))
            .build();

    Sample roundTripResult = parse(Sample.getDefaultInstance(), SampleMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  @Test
  void compareValueTypeMarshaling() {
    ValueTypeData input = ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE);
    ValueType builderResult =
        ValueType.newBuilder()
            .setType(1)
            .setUnit(2)
            .setAggregationTemporality(
                io.opentelemetry.proto.profiles.v1experimental.AggregationTemporality
                    .AGGREGATION_TEMPORALITY_CUMULATIVE)
            .build();

    ValueType roundTripResult =
        parse(ValueType.getDefaultInstance(), ValueTypeMarshaler.create(input));
    assertThat(roundTripResult).isEqualTo(builderResult);
  }

  // twin of sampleProfileBuilder
  private static ProfileData sampleProfileData() {
    return ImmutableProfileData.create(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        listOf(1L, 2L),
        Collections.emptyList(),
        Attributes.empty(),
        Collections.emptyList(),
        Collections.emptyList(),
        listOf("foo", "bar"),
        3,
        4,
        5,
        6,
        ImmutableValueTypeData.create(1, 2, AggregationTemporality.CUMULATIVE),
        7,
        listOf(8L, 9L),
        10);
  }

  // twin of sampleProfileData
  private static Profile.Builder sampleProfileBuilder() {
    return Profile.newBuilder()
        .addAllLocationIndices(listOf(1L, 2L))
        .setDropFrames(3)
        .setKeepFrames(4)
        .setTimeNanos(5)
        .setDurationNanos(6)
        .setPeriod(7)
        .setPeriodType(
            ValueType.newBuilder()
                .setType(1)
                .setUnit(2)
                .setAggregationTemporality(
                    io.opentelemetry.proto.profiles.v1experimental.AggregationTemporality
                        .AGGREGATION_TEMPORALITY_CUMULATIVE)
                .build())
        .addAllComment(listOf(8L, 9L))
        .addStringTable("foo")
        .addStringTable("bar")
        .setDefaultSampleType(10);
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
