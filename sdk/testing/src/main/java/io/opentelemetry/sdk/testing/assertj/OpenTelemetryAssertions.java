/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

/**
 * Entry point for assertion methods for OpenTelemetry types. To use type-specific assertions,
 * static import any {@code assertThat} method in this class instead of {@code
 * Assertions.assertThat}.
 */
public final class OpenTelemetryAssertions extends Assertions {

  /** Returns an assertion for {@link Attributes}. */
  public static AttributesAssert assertThat(@Nullable Attributes attributes) {
    return new AttributesAssert(attributes);
  }

  /** Returns an assertion for {@link SpanData}. */
  public static SpanDataAssert assertThat(@Nullable SpanData spanData) {
    return new SpanDataAssert(spanData);
  }

  /**
   * Returns an assertion for {@link MetricData}.
   *
   * @since 1.14.0
   */
  public static MetricAssert assertThat(@Nullable MetricData metricData) {
    return new MetricAssert(metricData);
  }

  /** Returns an assertion for {@link EventDataAssert}. */
  public static EventDataAssert assertThat(@Nullable EventData eventData) {
    return new EventDataAssert(eventData);
  }

  /**
   * Returns an attribute entry with a String value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<String>, String> attributeEntry(String key, String value) {
    return new AbstractMap.SimpleImmutableEntry<>(AttributeKey.stringKey(key), value);
  }

  /**
   * Returns an attribute entry with a boolean value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<Boolean>, Boolean> attributeEntry(
      String key, boolean value) {
    return new AbstractMap.SimpleImmutableEntry<>(AttributeKey.booleanKey(key), value);
  }

  /**
   * Returns an attribute entry with a long value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<Long>, Long> attributeEntry(String key, long value) {
    return new AbstractMap.SimpleImmutableEntry<>(AttributeKey.longKey(key), value);
  }

  /**
   * Returns an attribute entry with a double value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<Double>, Double> attributeEntry(String key, double value) {
    return new AbstractMap.SimpleImmutableEntry<>(AttributeKey.doubleKey(key), value);
  }

  /**
   * Returns an attribute entry with a String array value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<List<String>>, List<String>> attributeEntry(
      String key, String... value) {
    return new AbstractMap.SimpleImmutableEntry<>(
        AttributeKey.stringArrayKey(key), Arrays.asList(value));
  }

  /**
   * Returns an attribute entry with a boolean array value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<List<Boolean>>, List<Boolean>> attributeEntry(
      String key, boolean... value) {
    return new AbstractMap.SimpleImmutableEntry<>(AttributeKey.booleanArrayKey(key), toList(value));
  }

  /**
   * Returns an attribute entry with a long array value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<List<Long>>, List<Long>> attributeEntry(
      String key, long... value) {
    return new AbstractMap.SimpleImmutableEntry<>(
        AttributeKey.longArrayKey(key), Arrays.stream(value).boxed().collect(Collectors.toList()));
  }

  /**
   * Returns an attribute entry with a double array value for use with {@link
   * AttributesAssert#containsOnly(java.util.Map.Entry[])}.
   */
  public static Map.Entry<AttributeKey<List<Double>>, List<Double>> attributeEntry(
      String key, double... value) {
    return new AbstractMap.SimpleImmutableEntry<>(
        AttributeKey.doubleArrayKey(key),
        Arrays.stream(value).boxed().collect(Collectors.toList()));
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<String> key, StringAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<Boolean> key, BooleanAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(AttributeKey<Long> key, LongAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<Double> key, DoubleAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  // Will require a cast only if using a custom implementation of AttributeKey which is highly
  // unusual usage.
  @SuppressWarnings("FunctionalInterfaceClash")
  public static AttributeAssertion satisfies(
      AttributeKey<List<String>> key, StringListAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<List<Boolean>> key, BooleanListAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<List<Long>> key, LongListAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with a
   * value satisfying {@code assertion}.
   */
  public static AttributeAssertion satisfies(
      AttributeKey<List<Double>> key, DoubleListAssertConsumer assertion) {
    return AttributeAssertion.create(key, assertion);
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with the
   * given {@code value}.
   */
  public static <T> AttributeAssertion equalTo(AttributeKey<T> key, T value) {
    return AttributeAssertion.create(key, val -> val.isEqualTo(value));
  }

  /**
   * Returns an {@link AttributeAssertion} that asserts the given {@code key} is present with the
   * given {@code value}.
   */
  public static AttributeAssertion equalTo(AttributeKey<Long> key, int value) {
    return equalTo(key, (long) value);
  }

  // Unique interfaces to prevent generic functional interface clash. These are not interesting at
  // all but are required to be able to use the same method name in methods like satisfies above.

  public interface StringAssertConsumer extends Consumer<AbstractStringAssert<?>> {}

  public interface BooleanAssertConsumer extends Consumer<AbstractBooleanAssert<?>> {}

  public interface LongAssertConsumer extends Consumer<AbstractLongAssert<?>> {}

  public interface DoubleAssertConsumer extends Consumer<AbstractDoubleAssert<?>> {}

  public interface StringListAssertConsumer extends Consumer<ListAssert<String>> {}

  public interface BooleanListAssertConsumer extends Consumer<ListAssert<Boolean>> {}

  public interface LongListAssertConsumer extends Consumer<ListAssert<Long>> {}

  public interface DoubleListAssertConsumer extends Consumer<ListAssert<Double>> {}

  private static List<Boolean> toList(boolean... values) {
    Boolean[] boxed = new Boolean[values.length];
    for (int i = 0; i < values.length; i++) {
      boxed[i] = values[i];
    }
    return Arrays.asList(boxed);
  }

  private OpenTelemetryAssertions() {}
}
