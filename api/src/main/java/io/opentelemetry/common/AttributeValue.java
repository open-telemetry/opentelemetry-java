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

import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 4 types
 * of values: {@code String}, {@code boolean}, {@code long} or {@code double}, represented through
 * {@code AttributeValue.Type}.
 *
 * @since 0.1.0
 */
@Immutable
public interface AttributeValue {

  /**
   * An enum that represents all the possible value types for an {@code AttributeValue}.
   *
   * @since 0.1.0
   */
  enum Type {
    STRING,
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING_ARRAY,
    BOOLEAN_ARRAY,
    LONG_ARRAY,
    DOUBLE_ARRAY
  }

  class Factory {
    private Factory() {}

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
  }

  /**
   * Returns the string value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  String getStringValue();

  /**
   * Returns the boolean value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#BOOLEAN}.
   *
   * @return the boolean value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  boolean getBooleanValue();

  /**
   * Returns the long value of this {@code AttributeValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link Type#LONG}.
   *
   * @return the long value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  long getLongValue();

  /**
   * Returns the double value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#DOUBLE}.
   *
   * @return the double value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  double getDoubleValue();

  /**
   * Returns the String array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#STRING_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  List<String> getStringArrayValue();

  /**
   * Returns the boolean array value of this {@code AttributeValue}. An
   * UnsupportedOperationException will be thrown if getType() is not {@link Type#BOOLEAN_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  List<Boolean> getBooleanArrayValue();

  /**
   * Returns the long array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#LONG_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  List<Long> getLongArrayValue();

  /**
   * Returns the double array value of this {@code AttributeValue}. An UnsupportedOperationException
   * will be thrown if getType() is not {@link Type#DOUBLE_ARRAY}.
   *
   * @return the array values of this {@code AttributeValue}.
   * @since 0.3.0
   */
  List<Double> getDoubleArrayValue();

  /**
   * Returns a {@code Type} corresponding to the underlying value of this {@code AttributeValue}.
   *
   * @return the {@code Type} for the value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  Type getType();

  /**
   * Returns {@code true} if the {@code AttributeValue} contains a {@code null} value.
   *
   * @return {@code true} if the {@code AttributeValue} contains a {@code null} value.
   * @since 0.8.0
   */
  boolean isNull();
}
