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

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for a {@link Resource}. A resource can have 4
 * types of values: {@code String}, {@code boolean}, {@code long} (int64) or {@code double},
 * represented through {@code ResourceValue.Type}.
 *
 * @since 0.3.0
 */
@Immutable
public abstract class ResourceValue {

  /**
   * An enum that represents all the possible value types for a {@code Resource}.
   *
   * @since 0.3.0
   */
  public enum Type {
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING
  }

  ResourceValue() {}

  /**
   * Returns a {@code Type} corresponding to the underlying value of this {@code ResourceValue}.
   *
   * @return the {@code Type} for the value of this {@code ResourceValue}.
   * @since 0.3.0
   */
  public abstract Type getType();

  /**
   * Returns the string value of this {@code ResourceValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link ResourceValue.Type#STRING}.
   *
   * @return the string value of this {@code ResourceValue}.
   * @since 0.3.0
   */
  public String getStringValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the boolean value of this {@code ResourceValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link ResourceValue.Type#BOOLEAN}.
   *
   * @return the boolean value of this {@code ResourceValue}.
   * @since 0.3.0
   */
  public boolean getBooleanValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the long value of this {@code ResourceValue}. An UnsupportedOperationException will be
   * thrown if getType() is not {@link ResourceValue.Type#LONG}.
   *
   * @return the long value of this {@code ResourceValue}.
   * @since 0.3.0
   */
  public long getLongValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the double value of this {@code ResourceValue}. An UnsupportedOperationException will
   * be thrown if getType() is not {@link ResourceValue.Type#DOUBLE}.
   *
   * @return the double value of this {@code ResourceValue}.
   * @since 0.3.0
   */
  public double getDoubleValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns a {@code ResourceValue} with a {@code boolean} value.
   *
   * @param value The value for the resource.
   * @return A new {@code ResourceValue} containing a {@code boolean} value.
   */
  public static ResourceValue create(boolean value) {
    return ResourceValueBoolean.createBoolean(value);
  }

  /**
   * Returns a {@code ResourceValue} with a {@code long} value.
   *
   * @param value The value for the resource.
   * @return A new {@code ResourceValue} containing a {@code long} value.
   */
  public static ResourceValue create(long value) {
    return ResourceValueLong.createLong(value);
  }

  /**
   * Returns a {@code ResourceValue} with a {@code double} value.
   *
   * @param value The value for the resource.
   * @return A new {@code ResourceValue} containing a {@code double} value.
   */
  public static ResourceValue create(double value) {
    return ResourceValueDouble.createDouble(value);
  }

  /**
   * Returns a {@code ResourceValue} with a {@code String} value.
   *
   * @param value The value for the resource.
   * @return A new {@code ResourceValue} containing a {@code String} value.
   */
  public static ResourceValue create(String value) {
    return ResourceValueString.createString(value);
  }

  @Immutable
  @AutoValue
  abstract static class ResourceValueBoolean extends ResourceValue {

    ResourceValueBoolean() {}

    static ResourceValue createBoolean(boolean booleanValue) {
      return new AutoValue_ResourceValue_ResourceValueBoolean(booleanValue);
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
  abstract static class ResourceValueLong extends ResourceValue {

    ResourceValueLong() {}

    static ResourceValue createLong(long longValue) {
      return new AutoValue_ResourceValue_ResourceValueLong(longValue);
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
  abstract static class ResourceValueDouble extends ResourceValue {

    ResourceValueDouble() {}

    static ResourceValue createDouble(double doubleValue) {
      return new AutoValue_ResourceValue_ResourceValueDouble(doubleValue);
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
  abstract static class ResourceValueString extends ResourceValue {

    ResourceValueString() {}

    static ResourceValue createString(String stringValue) {
      return new AutoValue_ResourceValue_ResourceValueString(stringValue);
    }

    @Override
    public final Type getType() {
      return Type.STRING;
    }

    @Override
    public abstract String getStringValue();
  }
}
