/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.TraceStateBuilder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerRegistry;
import org.jeasy.random.randomizers.WordRandomizer;
import org.jeasy.random.randomizers.collection.ListRandomizer;
import org.jeasy.random.randomizers.collection.MapRandomizer;
import org.jeasy.random.randomizers.misc.BooleanRandomizer;
import org.jeasy.random.randomizers.number.ByteRandomizer;
import org.jeasy.random.randomizers.number.DoubleRandomizer;
import org.jeasy.random.randomizers.number.LongRandomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.jeasy.random.randomizers.range.LongRangeRandomizer;
import org.jeasy.random.randomizers.text.StringRandomizer;

final class SpanDataRandomizerRegistry implements RandomizerRegistry {

  private static final String HEX_ALPHABET = "0123456789abcdef";

  static final RandomizerRegistry INSTANCE = new SpanDataRandomizerRegistry();

  private EasyRandomParameters parameters;

  private EasyRandom easyRandom;

  private BooleanRandomizer booleanRandomizer;
  private ByteRandomizer byteRandomizer;
  private IntegerRangeRandomizer collectionSizeRandomizer;
  private DoubleRandomizer doubleRandomizer;
  private LongRandomizer longRandomizer;
  private StringRandomizer stringRandomizer;
  private IntegerRangeRandomizer unsignedInt32Randomizer;
  private LongRangeRandomizer unsignedInt64Randomizer;

  private AttributesRandomizer attributesRandomizer;
  private Map<AttributeType, Supplier<Randomizer<?>>> attributeValueRandomizers;
  private SpanContextRandomizer spanContextRandomizer;
  private TraceStateRandomizer traceStateRandomizer;

  private AutoValueRandomizer<EventData> eventDataRandomizer;
  private AutoValueRandomizer<InstrumentationLibraryInfo> instrumentationLibraryInfoRandomizer;
  private AutoValueRandomizer<LinkData> linkDataRandomizer;
  private AutoValueRandomizer<Resource> resourceRandomizer;
  private AutoValueRandomizer<SpanData> spanDataRandomizer;
  private AutoValueRandomizer<StatusData> statusDataRandomizer;

  @Override
  public void init(EasyRandomParameters parameters) {
    this.parameters = parameters;
  }

  // new EasyRandom() calls init, so we can't create it within init, or it will infinitely recurse.
  private void setup() {
    easyRandom = new EasyRandom(parameters);

    booleanRandomizer = new BooleanRandomizer(parameters.getSeed());
    byteRandomizer = new ByteRandomizer(parameters.getSeed());
    collectionSizeRandomizer =
        new IntegerRangeRandomizer(
            parameters.getCollectionSizeRange().getMin(),
            parameters.getCollectionSizeRange().getMax(),
            parameters.getSeed());
    doubleRandomizer = new DoubleRandomizer(parameters.getSeed());
    longRandomizer = new LongRandomizer(parameters.getSeed());
    stringRandomizer =
        new StringRandomizer(
            StandardCharsets.UTF_8,
            parameters.getStringLengthRange().getMin(),
            parameters.getStringLengthRange().getMax(),
            parameters.getSeed());
    unsignedInt32Randomizer =
        new IntegerRangeRandomizer(0, Integer.MAX_VALUE, parameters.getSeed());
    unsignedInt64Randomizer = new LongRangeRandomizer(0L, Long.MAX_VALUE, parameters.getSeed());

    attributeValueRandomizers = new EnumMap<>(AttributeType.class);
    for (AttributeType type : AttributeType.values()) {
      switch (type) {
        case STRING:
          attributeValueRandomizers.put(type, () -> stringRandomizer);
          break;
        case BOOLEAN:
          attributeValueRandomizers.put(type, () -> booleanRandomizer);
          break;
        case LONG:
          attributeValueRandomizers.put(type, () -> longRandomizer);
          break;
        case DOUBLE:
          attributeValueRandomizers.put(type, () -> doubleRandomizer);
          break;
        case STRING_ARRAY:
          attributeValueRandomizers.put(
              type,
              () ->
                  new ListRandomizer<>(
                      stringRandomizer, collectionSizeRandomizer.getRandomValue()));
          break;
        case BOOLEAN_ARRAY:
          attributeValueRandomizers.put(
              type,
              () ->
                  new ListRandomizer<>(
                      booleanRandomizer, collectionSizeRandomizer.getRandomValue()));
          break;
        case LONG_ARRAY:
          attributeValueRandomizers.put(
              type,
              () ->
                  new ListRandomizer<>(longRandomizer, collectionSizeRandomizer.getRandomValue()));
          break;
        case DOUBLE_ARRAY:
          attributeValueRandomizers.put(
              type,
              () ->
                  new ListRandomizer<>(
                      doubleRandomizer, collectionSizeRandomizer.getRandomValue()));
          break;
      }
    }

    attributesRandomizer = new AttributesRandomizer();
    eventDataRandomizer =
        new AutoValueRandomizer<>("io.opentelemetry.sdk.trace.data.AutoValue_ImmutableEventData");
    instrumentationLibraryInfoRandomizer =
        new AutoValueRandomizer<>(
            "io.opentelemetry.sdk.common.AutoValue_InstrumentationLibraryInfo");
    linkDataRandomizer =
        new AutoValueRandomizer<>("io.opentelemetry.sdk.trace.data.AutoValue_ImmutableLinkData");
    resourceRandomizer =
        new AutoValueRandomizer<>("io.opentelemetry.sdk.resources.AutoValue_Resource");
    spanContextRandomizer = new SpanContextRandomizer();
    spanDataRandomizer =
        new AutoValueRandomizer<>("io.opentelemetry.sdk.testing.trace.AutoValue_TestSpanData");
    statusDataRandomizer =
        new AutoValueRandomizer<>("io.opentelemetry.sdk.trace.data.AutoValue_ImmutableStatusData");
    traceStateRandomizer = new TraceStateRandomizer();
  }

  @Override
  @Nullable
  public Randomizer<?> getRandomizer(Field field) {
    if (field.getName().contains("Count") || field.getName().contains("totalRecorded")) {
      return unsignedInt32Randomizer;
    }

    if (field.getName().contains("Nanos")) {
      return unsignedInt64Randomizer;
    }

    // TODO(anuraaga): Make work for autovalue, unfortunately it only adds Nullable to constructor
    // parameters / getters but not to fields.
    // https://github.com/open-telemetry/opentelemetry-java/issues/3498
    if (field.getAnnotation(Nullable.class) == null) {
      return null;
    }

    Randomizer<?> delegate = getRandomizer(field.getType());
    if (delegate == null) {
      return null;
    }

    return new NullableRandomizer<>(delegate);
  }

  @Override
  @Nullable
  public Randomizer<?> getRandomizer(Class<?> type) {
    if (easyRandom == null) {
      setup();
    }

    if (type == Attributes.class) {
      return attributesRandomizer;
    }
    if (type == EventData.class) {
      return eventDataRandomizer;
    }
    if (type == InstrumentationLibraryInfo.class) {
      return instrumentationLibraryInfoRandomizer;
    }
    if (type == LinkData.class) {
      return linkDataRandomizer;
    }
    if (type == Resource.class) {
      return resourceRandomizer;
    }
    if (type == SpanContext.class) {
      return spanContextRandomizer;
    }
    if (type == SpanData.class) {
      return spanDataRandomizer;
    }
    if (type == StatusData.class) {
      return statusDataRandomizer;
    }
    if (type == TraceState.class) {
      return traceStateRandomizer;
    }

    return null;
  }

  private final class AttributesRandomizer implements Randomizer<Attributes> {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Attributes getRandomValue() {
      AttributesBuilder attributes = Attributes.builder();

      int numAttributes = collectionSizeRandomizer.getRandomValue();
      for (int i = 0; i < numAttributes; i++) {
        AttributeType type = easyRandom.nextObject(AttributeType.class);
        attributes.put(
            (AttributeKey) randomKey(type),
            attributeValueRandomizers.get(type).get().getRandomValue());
      }

      return attributes.build();
    }

    private AttributeKey<?> randomKey(AttributeType type) {
      String key = stringRandomizer.getRandomValue();
      switch (type) {
        case STRING:
          return AttributeKey.stringKey(key);
        case BOOLEAN:
          return AttributeKey.booleanKey(key);
        case LONG:
          return AttributeKey.longKey(key);
        case DOUBLE:
          return AttributeKey.doubleKey(key);
        case STRING_ARRAY:
          return AttributeKey.stringArrayKey(key);
        case BOOLEAN_ARRAY:
          return AttributeKey.booleanArrayKey(key);
        case LONG_ARRAY:
          return AttributeKey.longArrayKey(key);
        case DOUBLE_ARRAY:
          return AttributeKey.doubleArrayKey(key);
      }
      throw new IllegalStateException("Unexpected value: " + type);
    }
  }

  private final class NullableRandomizer<T> implements Randomizer<T> {
    private final Randomizer<T> delegate;

    NullableRandomizer(Randomizer<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    @Nullable
    public T getRandomValue() {
      boolean isNull = booleanRandomizer.getRandomValue();
      if (isNull) {
        return null;
      } else {
        return delegate.getRandomValue();
      }
    }
  }

  private final class SpanContextRandomizer implements Randomizer<SpanContext> {
    @Override
    public SpanContext getRandomValue() {
      boolean isInvalid = booleanRandomizer.getRandomValue();
      if (isInvalid) {
        return SpanContext.getInvalid();
      }
      String traceId = randomHex(TraceId.getLength());
      String spanId = randomHex(SpanId.getLength());
      TraceFlags traceFlags = TraceFlags.fromByte(byteRandomizer.getRandomValue());
      TraceState traceState = easyRandom.nextObject(TraceState.class);
      boolean isRemote = booleanRandomizer.getRandomValue();
      if (isRemote) {
        return SpanContext.createFromRemoteParent(traceId, spanId, traceFlags, traceState);
      }
      return SpanContext.create(traceId, spanId, traceFlags, traceState);
    }
  }

  private final class TraceStateRandomizer implements Randomizer<TraceState> {

    // Lorem ipsum - should generate values that are valid for trace state.
    private final WordRandomizer wordRandomizer;

    TraceStateRandomizer() {
      wordRandomizer = new WordRandomizer(parameters.getSeed());
    }

    @Override
    public TraceState getRandomValue() {
      // Don't memoize so it always has a random size.
      MapRandomizer<String, String> stringMapRandomizer =
          new MapRandomizer<>(
              wordRandomizer, wordRandomizer, collectionSizeRandomizer.getRandomValue());
      TraceStateBuilder traceState = TraceState.builder();
      stringMapRandomizer.getRandomValue().forEach(traceState::put);
      return traceState.build();
    }
  }

  // EasyRandom can only automatically instantiate public implementation classes. Because our
  // AutoValue implementations are private, we have to point it at the implementation class using
  // reflection.
  private final class AutoValueRandomizer<T> implements Randomizer<T> {

    private final String autoValueClassName;

    AutoValueRandomizer(String autoValueClassName) {
      this.autoValueClassName = autoValueClassName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRandomValue() {
      try {
        return easyRandom.nextObject((Class<? extends T>) Class.forName(autoValueClassName));
      } catch (ClassNotFoundException e) {
        throw new AssertionError(e);
      }
    }
  }

  private String randomHex(int length) {
    char[] chars = new char[length];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = HEX_ALPHABET.charAt(easyRandom.nextInt(HEX_ALPHABET.length()));
    }
    return new String(chars);
  }
}
