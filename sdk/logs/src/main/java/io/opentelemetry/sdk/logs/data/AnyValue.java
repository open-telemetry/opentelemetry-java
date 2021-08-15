/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for a data body. An {@code AnyValue} can have 6
 * types of values: {@code String}, {@code boolean}, {@code int}, {@code double}, {@code array}, or
 * {@code kvlist}. represented through {@code AnyValue.Type}. A {@code array} or a {@code kvlist}
 * can in turn hold other {@code AnyValue} instances, allowing for mapping to JSON-like structures.
 */
@Immutable
public abstract class AnyValue {

  /** An enum that represents all the possible value types for an {@code AnyValue}. */
  public enum Type {
    STRING,
    BOOL,
    INT64,
    DOUBLE,
    ARRAY,
    KVLIST
  }

  /**
   * Returns an {@code AnyValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AnyValue} with a string value.
   */
  public static AnyValue stringAnyValue(String stringValue) {
    return AnyValueString.create(stringValue);
  }

  /**
   * Returns the string value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   */
  public String getStringValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with an int value.
   *
   * @param longValue The new value.
   * @return an {@code AnyValue} with a int value.
   */
  public static AnyValue longAnyValue(long longValue) {
    return AnyValueLong.create(longValue);
  }

  public long getLongValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with a bool value.
   *
   * @param boolValue The new value.
   * @return an {@code AnyValue} with a bool value.
   */
  public static AnyValue boolAnyValue(boolean boolValue) {
    return AnyValueBool.create(boolValue);
  }

  /**
   * Returns the boolean value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#BOOL}.
   *
   * @return the boolean value of this {@code AttributeValue}.
   */
  public boolean getBoolValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with a double value.
   *
   * @param doubleValue The new value.
   * @return an {@code AnyValue} with a double value.
   */
  public static AnyValue doubleAnyValue(double doubleValue) {
    return AnyValueDouble.create(doubleValue);
  }

  /**
   * Returns the double value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#DOUBLE}.
   *
   * @return the double value of this {@code AttributeValue}.
   */
  public double getDoubleValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with a array value.
   *
   * @param values The new value.
   * @return an {@code AnyValue} with a array value.
   */
  public static AnyValue arrayAnyValue(List<AnyValue> values) {
    return AnyValueArray.create(values);
  }

  /**
   * Returns the array value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#ARRAY}.
   *
   * @return the array value of this {@code AttributeValue}.
   */
  public List<AnyValue> getArrayValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with a kvlist value.
   *
   * @param values The new value.
   * @return an {@code AnyValue} with a kvlist value.
   */
  public static AnyValue kvlistAnyValue(Map<String, AnyValue> values) {
    return AnyValueKvlist.create(values);
  }

  /**
   * Returns the string value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   */
  public Map<String, AnyValue> getKvlistValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  public abstract Type getType();

  @Immutable
  @AutoValue
  abstract static class AnyValueString extends AnyValue {
    AnyValueString() {}

    static AnyValue create(String stringValue) {
      return new AutoValue_AnyValue_AnyValueString(stringValue);
    }

    @Override
    public final Type getType() {
      return Type.STRING;
    }

    @Override
    @Nullable
    public abstract String getStringValue();
  }

  @Immutable
  @AutoValue
  abstract static class AnyValueLong extends AnyValue {
    AnyValueLong() {}

    static AnyValue create(long longValue) {
      return new AutoValue_AnyValue_AnyValueLong(longValue);
    }

    @Override
    public final Type getType() {
      return Type.INT64;
    }

    @Override
    public abstract long getLongValue();
  }

  @Immutable
  @AutoValue
  abstract static class AnyValueBool extends AnyValue {
    AnyValueBool() {}

    static AnyValue create(boolean boolValue) {
      return new AutoValue_AnyValue_AnyValueBool(boolValue);
    }

    @Override
    public final Type getType() {
      return Type.BOOL;
    }

    @Override
    public abstract boolean getBoolValue();
  }

  @Immutable
  @AutoValue
  abstract static class AnyValueDouble extends AnyValue {
    AnyValueDouble() {}

    static AnyValue create(double doubleValue) {
      return new AutoValue_AnyValue_AnyValueDouble(doubleValue);
    }

    @Override
    public final Type getType() {
      return Type.DOUBLE;
    }

    @Override
    public abstract double getDoubleValue();
  }

  @Immutable
  @AutoValue
  abstract static class AnyValueArray extends AnyValue {
    AnyValueArray() {}

    static AnyValue create(List<AnyValue> arrayValue) {
      return new AutoValue_AnyValue_AnyValueArray(arrayValue);
    }

    @Override
    public final Type getType() {
      return Type.ARRAY;
    }

    @Override
    public abstract List<AnyValue> getArrayValue();
  }

  @Immutable
  @AutoValue
  abstract static class AnyValueKvlist extends AnyValue {
    AnyValueKvlist() {}

    static AnyValue create(Map<String, AnyValue> kvlistValue) {
      return new AutoValue_AnyValue_AnyValueKvlist(kvlistValue);
    }

    @Override
    public final Type getType() {
      return Type.KVLIST;
    }

    @Override
    public abstract Map<String, AnyValue> getKvlistValue();
  }
}
