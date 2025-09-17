/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Marshaler utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class StatelessMarshalerUtil {
  private static final MarshalerContext.Key GROUPER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key ATTRIBUTES_SIZE_CALCULATOR_KEY = MarshalerContext.key();

  /** Groups SDK items by resource and instrumentation scope. */
  public static <T> Map<Resource, Map<InstrumentationScopeInfo, List<T>>> groupByResourceAndScope(
      Collection<T> dataList,
      Function<T, Resource> getResource,
      Function<T, InstrumentationScopeInfo> getInstrumentationScope,
      MarshalerContext context) {
    Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result = context.getIdentityMap();

    Grouper<T> grouper = context.getInstance(GROUPER_KEY, Grouper::new);
    grouper.initialize(result, getResource, getInstrumentationScope, context);
    dataList.forEach(grouper);

    return result;
  }

  private static class Grouper<T> implements Consumer<T> {
    @SuppressWarnings("NullAway")
    private Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result;

    @SuppressWarnings("NullAway")
    private Function<T, Resource> getResource;

    @SuppressWarnings("NullAway")
    private Function<T, InstrumentationScopeInfo> getInstrumentationScope;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        Map<Resource, Map<InstrumentationScopeInfo, List<T>>> result,
        Function<T, Resource> getResource,
        Function<T, InstrumentationScopeInfo> getInstrumentationScope,
        MarshalerContext context) {
      this.result = result;
      this.getResource = getResource;
      this.getInstrumentationScope = getInstrumentationScope;
      this.context = context;
    }

    @Override
    public void accept(T data) {
      Resource resource = getResource.apply(data);
      Map<InstrumentationScopeInfo, List<T>> scopeInfoListMap = result.get(resource);
      if (scopeInfoListMap == null) {
        scopeInfoListMap = context.getIdentityMap();
        result.put(resource, scopeInfoListMap);
      }
      InstrumentationScopeInfo instrumentationScopeInfo = getInstrumentationScope.apply(data);
      List<T> elementList = scopeInfoListMap.get(instrumentationScopeInfo);
      if (elementList == null) {
        elementList = context.getList();
        scopeInfoListMap.put(instrumentationScopeInfo, elementList);
      }
      elementList.add(data);
    }
  }

  /**
   * Returns the size of a string field. This method adds elements to context, use together with
   * {@link Serializer#serializeStringWithContext(ProtoFieldInfo, String, MarshalerContext)}.
   */
  public static int sizeStringWithContext(
      ProtoFieldInfo field, @Nullable String value, MarshalerContext context) {
    if (value == null || value.isEmpty()) {
      return sizeBytes(field, 0);
    }
    if (context.marshalStringNoAllocation()) {
      int utf8Size = context.getStringEncoder().getUtf8Size(value);
      context.addSize(utf8Size);
      return sizeBytes(field, utf8Size);
    } else {
      byte[] valueUtf8 = MarshalerUtil.toBytes(value);
      context.addData(valueUtf8);
      return sizeBytes(field, valueUtf8.length);
    }
  }

  /** Returns the size of a bytes field. */
  private static int sizeBytes(ProtoFieldInfo field, int length) {
    if (length == 0) {
      return 0;
    }
    return field.getTagSize() + CodedOutputStream.computeLengthDelimitedFieldSize(length);
  }

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, List,
   * StatelessMarshaler, MarshalerContext)}.
   */
  public static <T> int sizeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      List<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context) {
    if (messages.isEmpty()) {
      return 0;
    }

    int size = 0;
    int fieldTagSize = field.getTagSize();
    for (int i = 0; i < messages.size(); i++) {
      T message = messages.get(i);
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(message, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
    return size;
  }

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Collection,
   * StatelessMarshaler, MarshalerContext, MarshalerContext.Key)}.
   */
  @SuppressWarnings("unchecked")
  public static <T> int sizeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Collection<? extends T> messages,
      StatelessMarshaler<T> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key) {
    if (messages instanceof List) {
      return sizeRepeatedMessageWithContext(field, (List<T>) messages, marshaler, context);
    }

    if (messages.isEmpty()) {
      return 0;
    }

    RepeatedElementSizeCalculator<T> sizeCalculator =
        context.getInstance(key, RepeatedElementSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    messages.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Map,
   * StatelessMarshaler2, MarshalerContext, MarshalerContext.Key)}.
   */
  public static <K, V> int sizeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Map<K, V> messages,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context,
      MarshalerContext.Key key) {
    if (messages.isEmpty()) {
      return 0;
    }

    RepeatedElementPairSizeCalculator<K, V> sizeCalculator =
        context.getInstance(key, RepeatedElementPairSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    messages.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  /**
   * Returns the size of a repeated message field. This method adds elements to context, use
   * together with {@link Serializer#serializeRepeatedMessageWithContext(ProtoFieldInfo, Attributes,
   * StatelessMarshaler2, MarshalerContext)}.
   */
  public static int sizeRepeatedMessageWithContext(
      ProtoFieldInfo field,
      Attributes attributes,
      StatelessMarshaler2<AttributeKey<?>, Object> marshaler,
      MarshalerContext context) {
    if (attributes.isEmpty()) {
      return 0;
    }

    RepeatedElementPairSizeCalculator<AttributeKey<?>, Object> sizeCalculator =
        context.getInstance(ATTRIBUTES_SIZE_CALCULATOR_KEY, RepeatedElementPairSizeCalculator::new);
    sizeCalculator.initialize(field, marshaler, context);
    attributes.forEach(sizeCalculator);

    return sizeCalculator.size;
  }

  private static class RepeatedElementSizeCalculator<T> implements Consumer<T> {
    private int size;
    private int fieldTagSize;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler<T> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field, StatelessMarshaler<T> marshaler, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(T element) {
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(element, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }

  private static class RepeatedElementPairSizeCalculator<K, V> implements BiConsumer<K, V> {
    private int size;
    private int fieldTagSize;

    @SuppressWarnings("NullAway")
    private StatelessMarshaler2<K, V> marshaler;

    @SuppressWarnings("NullAway")
    private MarshalerContext context;

    void initialize(
        ProtoFieldInfo field, StatelessMarshaler2<K, V> marshaler, MarshalerContext context) {
      this.size = 0;
      this.fieldTagSize = field.getTagSize();
      this.marshaler = marshaler;
      this.context = context;
    }

    @Override
    public void accept(K key, V value) {
      int sizeIndex = context.addSize();
      int fieldSize = marshaler.getBinarySerializedSize(key, value, context);
      context.setSize(sizeIndex, fieldSize);
      size += fieldTagSize + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    }
  }

  /**
   * Returns the size of a message field. This method adds elements to context, use together with
   * {@link Serializer#serializeMessageWithContext(ProtoFieldInfo, Object, StatelessMarshaler,
   * MarshalerContext)}.
   */
  public static <T> int sizeMessageWithContext(
      ProtoFieldInfo field, T element, StatelessMarshaler<T> marshaler, MarshalerContext context) {
    int sizeIndex = context.addSize();
    int fieldSize = marshaler.getBinarySerializedSize(element, context);
    int size = field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    context.setSize(sizeIndex, fieldSize);
    return size;
  }

  /**
   * Returns the size of a message field. This method adds elements to context, use together with
   * {@link Serializer#serializeMessageWithContext(ProtoFieldInfo, Object, Object,
   * StatelessMarshaler2, MarshalerContext)}.
   */
  public static <K, V> int sizeMessageWithContext(
      ProtoFieldInfo field,
      K key,
      V value,
      StatelessMarshaler2<K, V> marshaler,
      MarshalerContext context) {
    int sizeIndex = context.addSize();
    int fieldSize = marshaler.getBinarySerializedSize(key, value, context);
    int size = field.getTagSize() + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
    context.setSize(sizeIndex, fieldSize);
    return size;
  }

  private StatelessMarshalerUtil() {}
}
