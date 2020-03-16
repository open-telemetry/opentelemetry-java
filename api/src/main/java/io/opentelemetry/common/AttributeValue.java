/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.common;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 4 types
 * of values: {@code String}, {@code boolean}, {@code long} or {@code double}, represented through
 * {@code AttributeValue.Type}.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class AttributeValue {

  /**
   * An enum that represents all the possible value types for an {@code AttributeValue}.
   *
   * @since 0.1.0
   */
  public enum Type {
    STRING,
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING_ARRAY,
    BOOLEAN_ARRAY,
    LONG_ARRAY,
    DOUBLE_ARRAY
  }

  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @since 0.1.0
   */
  public static AttributeValue stringAttributeValue(String stringValue) {
    return AttributeValueString.create(stringValue);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean value.
   *
   * @param booleanValue The new value.
   * @return an {@code AttributeValue} with a boolean value.
   * @since 0.1.0
   */
  public static AttributeValue booleanAttributeValue(boolean booleanValue) {
    return AttributeValueBoolean.create(booleanValue);
  }

  /**
   * Returns an {@code AttributeValue} with a long value.
   *
   * @param longValue The new value.
   * @return an {@code AttributeValue} with a long value.
   * @since 0.1.0
   */
  public static AttributeValue longAttributeValue(long longValue) {
    return AttributeValueLong.create(longValue);
  }

  /**
   * Returns an {@code AttributeValue} with a double value.
   *
   * @param doubleValue The new value.
   * @return an {@code AttributeValue} with a double value.
   * @since 0.1.0
   */
  public static AttributeValue doubleAttributeValue(double doubleValue) {
    return AttributeValueDouble.create(doubleValue);
  }

  /**
   * Returns an {@code AttributeValue} with a String array value.
   *
   * @param stringValues The new values.
   * @return an {@code AttributeValue} with a String array value.
   * @since 0.3.0
   */
  public static AttributeValue arrayAttributeValue(String... stringValues) {
    return AttributeValueStringArray.create(stringValues);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean array value.
   *
   * @param booleanValues The new values.
   * @return an {@code AttributeValue} with a boolean array value.
   * @since 0.3.0
   */
  public static AttributeValue arrayAttributeValue(Boolean... booleanValues) {
    return AttributeValueBooleanArray.create(booleanValues);
  }

  /**
   * Returns an {@code AttributeValue} with a long array value.
   *
   * @param longValues The new values.
   * @return an {@code AttributeValue} with a long array value.
   * @since 0.3.0
   */
  public static AttributeValue arrayAttributeValue(Long... longValues) {
    return AttributeValueLongArray.create(longValues);
  }

  /**
   * Returns an {@code AttributeValue} with a double array value.
   *
   * @param doubleValues The new values.
   * @return an {@code AttributeValue} with a double array value.
   * @since 0.3.0
   */
  public static AttributeValue arrayAttributeValue(Double... doubleValues) {
    return AttributeValueDoubleArray.create(doubleValues);
  }

  AttributeValue() {}

  /**
   * Returns the string value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public String getStringValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the boolean value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#BOOLEAN}.
   *
   * @return the boolean value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public boolean getBooleanValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the long value of this {@code AttributeValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link Type#LONG}.
   *
   * @return the long value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public long getLongValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the double value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#DOUBLE}.
   *
   * @return the double value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public double getDoubleValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the String array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#STRING_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  public List<String> getStringArrayValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the boolean array value of this {@code AttributeValue}. An
   * UnsupportedOperationException will be thrown if getType() is not {@link Type#BOOLEAN_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  public List<Boolean> getBooleanArrayValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the long array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#LONG_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  public List<Long> getLongArrayValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the double array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#DOUBLE_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  public List<Double> getDoubleArrayValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns a {@code Type} corresponding to the underlying value of this {@code AttributeValue}.
   *
   * @return the {@code Type} for the value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public abstract Type getType();

  @Immutable
  @AutoValue
  abstract static class AttributeValueString extends AttributeValue {

    AttributeValueString() {}

    static AttributeValue create(String stringValue) {
      return new AutoValue_AttributeValue_AttributeValueString(stringValue);
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
  abstract static class AttributeValueBoolean extends AttributeValue {

    AttributeValueBoolean() {}

    static AttributeValue create(boolean booleanValue) {
      return new AutoValue_AttributeValue_AttributeValueBoolean(booleanValue);
    }

    @Override
    public final Type getType() {
      return Type.BOOLEAN;
    }

    @Override
    public abstract boolean getBooleanValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueLong extends AttributeValue {

    AttributeValueLong() {}

    static AttributeValue create(long longValue) {
      return new AutoValue_AttributeValue_AttributeValueLong(longValue);
    }

    @Override
    public final Type getType() {
      return Type.LONG;
    }

    @Override
    public abstract long getLongValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueDouble extends AttributeValue {

    AttributeValueDouble() {}

    static AttributeValue create(double doubleValue) {
      return new AutoValue_AttributeValue_AttributeValueDouble(doubleValue);
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
  abstract static class AttributeValueStringArray extends AttributeValue {

    AttributeValueStringArray() {}

    static AttributeValue create(String... stringValues) {
      if (stringValues == null) {
        return new AutoValue_AttributeValue_AttributeValueStringArray(
            Collections.<String>emptyList());
      }
      return new AutoValue_AttributeValue_AttributeValueStringArray(
          Collections.unmodifiableList(Arrays.asList(stringValues)));
    }

    @Override
    public final Type getType() {
      return Type.STRING_ARRAY;
    }

    @Override
    public abstract List<String> getStringArrayValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueBooleanArray extends AttributeValue {

    AttributeValueBooleanArray() {}

    static AttributeValue create(Boolean... booleanValues) {
      if (booleanValues == null) {
        return new AutoValue_AttributeValue_AttributeValueBooleanArray(
            Collections.<Boolean>emptyList());
      }
      List<Boolean> values = new ArrayList<>(booleanValues.length);
      for (Boolean value : booleanValues) {
        values.add(value);
      }
      return new AutoValue_AttributeValue_AttributeValueBooleanArray(
          Collections.unmodifiableList(values));
    }

    @Override
    public final Type getType() {
      return Type.BOOLEAN_ARRAY;
    }

    @Override
    public abstract List<Boolean> getBooleanArrayValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueLongArray extends AttributeValue {

    AttributeValueLongArray() {}

    static AttributeValue create(Long... longValues) {
      if (longValues == null) {
        return new AutoValue_AttributeValue_AttributeValueLongArray(Collections.<Long>emptyList());
      }
      List<Long> values = new ArrayList<>(longValues.length);
      for (Long value : longValues) {
        values.add(value);
      }
      return new AutoValue_AttributeValue_AttributeValueLongArray(
          Collections.unmodifiableList(values));
    }

    @Override
    public final Type getType() {
      return Type.LONG_ARRAY;
    }

    @Override
    public abstract List<Long> getLongArrayValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueDoubleArray extends AttributeValue {

    AttributeValueDoubleArray() {}

    static AttributeValue create(Double... doubleValues) {
      if (doubleValues == null) {
        return new AutoValue_AttributeValue_AttributeValueDoubleArray(
            Collections.<Double>emptyList());
      }
      List<Double> values = new ArrayList<>(doubleValues.length);
      for (Double value : doubleValues) {
        values.add(value);
      }
      return new AutoValue_AttributeValue_AttributeValueDoubleArray(
          Collections.unmodifiableList(values));
    }

    @Override
    public final Type getType() {
      return Type.DOUBLE_ARRAY;
    }

    @Override
    public abstract List<Double> getDoubleArrayValue();
  }
}
