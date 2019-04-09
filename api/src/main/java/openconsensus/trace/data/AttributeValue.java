/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.Utils;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 4 types
 * of values: {@code String}, {@code Boolean}, {@code Long} or {@code Double}, represented through
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
    DOUBLE
  }

  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @throws NullPointerException if {@code stringValue} is {@code null}.
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

  AttributeValue() {}

  /**
   * Returns the string value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#STRING}.
   *
   * @return the string value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public String getStringValue() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the boolean value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#BOOLEAN}.
   *
   * @return the boolean value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public Boolean getBooleanValue() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the long value of this {@code AttributeValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link Type#LONG}.
   *
   * @return the long value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public Long getLongValue() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the double value of this {@code AttributeValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link Type#DOUBLE}.
   *
   * @return the double value of this {@code AttributeValue}.
   * @since 0.1.0
   */
  public Double getDoubleValue() {
    throw new UnsupportedOperationException();
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
      return new AutoValue_AttributeValue_AttributeValueString(
          Utils.checkNotNull(stringValue, "stringValue"));
    }

    @Override
    public final Type getType() {
      return Type.STRING;
    }

    @Override
    public abstract String getStringValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueBoolean extends AttributeValue {

    AttributeValueBoolean() {}

    static AttributeValue create(Boolean booleanValue) {
      return new AutoValue_AttributeValue_AttributeValueBoolean(
          Utils.checkNotNull(booleanValue, "booleanValue"));
    }

    @Override
    public final Type getType() {
      return Type.BOOLEAN;
    }

    @Override
    public abstract Boolean getBooleanValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueLong extends AttributeValue {

    AttributeValueLong() {}

    static AttributeValue create(Long longValue) {
      return new AutoValue_AttributeValue_AttributeValueLong(
          Utils.checkNotNull(longValue, "longValue"));
    }

    @Override
    public final Type getType() {
      return Type.LONG;
    }

    @Override
    public abstract Long getLongValue();
  }

  @Immutable
  @AutoValue
  abstract static class AttributeValueDouble extends AttributeValue {

    AttributeValueDouble() {}

    static AttributeValue create(Double doubleValue) {
      return new AutoValue_AttributeValue_AttributeValueDouble(
          Utils.checkNotNull(doubleValue, "doubleValue"));
    }

    @Override
    public final Type getType() {
      return Type.DOUBLE;
    }

    @Override
    public abstract Double getDoubleValue();
  }
}
