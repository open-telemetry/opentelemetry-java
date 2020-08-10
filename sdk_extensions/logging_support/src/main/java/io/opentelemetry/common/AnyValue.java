/*
 * Copyright 2020, OpenTelemetry Authors
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
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for a data body. An {@code AnyValue} can have 6
 * types of values: {@code String}, {@code boolean}, {@code int}, {@code double}, {@code array}, or
 * {@code kvlist}. represented through {@code AnyValue.Type}. A {@code array} or a {@code kvlist}
 * can in turn hold other {@code AnyValue} instances, allowing for mapping to JSON-like structures.
 *
 * @since 0.7.0
 */
@Immutable
public abstract class AnyValue {

  /**
   * An enum that represents all the possible value types for an {@code AnyValue}.
   *
   * @since 0.7.0
   */
  public enum Type {
    STRING,
    BOOL,
    INT,
    DOUBLE,
    ARRAY,
    KVLIST
  }

  /**
   * Returns an {@code AnyValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AnyValue} with a string value.
   * @since 0.7.0
   */
  public static AnyValue stringAnyValue(String stringValue) {
    return AnyValueString.create(stringValue);
  }

  /**
   * Returns the string value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   * @since 0.7.0
   */
  public String getStringValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with an int value.
   *
   * @param intValue The new value.
   * @return an {@code AnyValue} with a int value.
   * @since 0.7.0
   */
  public static AnyValue intAnyValue(int intValue) {
    return AnyValueInt.create(intValue);
  }

  public int getIntValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns an {@code AnyValue} with a bool value.
   *
   * @param boolValue The new value.
   * @return an {@code AnyValue} with a bool value.
   * @since 0.7.0
   */
  public static AnyValue boolAnyValue(boolean boolValue) {
    return AnyValueBool.create(boolValue);
  }

  /**
   * Returns the boolean value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#BOOL}.
   *
   * @return the boolean value of this {@code AttributeValue}.
   * @since 0.7.0
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
   * @since 0.7.0
   */
  public static AnyValue doubleAnyValue(double doubleValue) {
    return AnyValueDouble.create(doubleValue);
  }

  /**
   * Returns the double value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#DOUBLE}.
   *
   * @return the double value of this {@code AttributeValue}.
   * @since 0.7.0
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
   * @since 0.7.0
   */
  public static AnyValue arrayAnyValue(List<AnyValue> values) {
    return AnyValueArray.create(values);
  }

  /**
   * Returns the array value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#ARRAY}.
   *
   * @return the array value of this {@code AttributeValue}.
   * @since 0.7.0
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
   * @since 0.7.0
   */
  public static AnyValue kvlistAnyValue(Map<String, AnyValue> values) {
    return AnyValueKvlist.create(values);
  }

  /**
   * Returns the string value of this {@code AnyValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link AnyValue.Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   * @since 0.7.0
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
  abstract static class AnyValueInt extends AnyValue {
    AnyValueInt() {}

    static AnyValue create(int intValue) {
      return new AutoValue_AnyValue_AnyValueInt(intValue);
    }

    @Override
    public final Type getType() {
      return Type.INT;
    }

    @Override
    public abstract int getIntValue();
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
