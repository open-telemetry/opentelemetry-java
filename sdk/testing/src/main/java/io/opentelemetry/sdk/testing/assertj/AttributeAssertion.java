/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/** An assertion on an attribute key. */
@AutoValue
public abstract class AttributeAssertion {

  // This method is not type-safe! But because the constructor is private, we know it will only be
  // created through our factories, which are type-safe.
  @SuppressWarnings("unchecked")
  static AttributeAssertion create(
      AttributeKey<?> key, Consumer<? extends AbstractAssert<?, ?>> assertion) {
    return new AutoValue_AttributeAssertion(key, (Consumer<AbstractAssert<?, ?>>) assertion);
  }

  abstract AttributeKey<?> getKey();

  abstract Consumer<AbstractAssert<?, ?>> getAssertion();

  // The return type of these assertions must match the parameters in methods like
  // OpenTelemetryAssertions.satisfies.
  // Our code is nullness annotated but assertj is not. NullAway seems to still treat the base class
  // of OpenTelemetryAssertions as annotated though, so there seems to be no way to avoid
  // suppressing here.
  @SuppressWarnings("NullAway")
  static AbstractAssert<?, ?> attributeValueAssertion(AttributeKey<?> key, @Nullable Object value) {
    AbstractAssert<? extends AbstractAssert<?, ?>, ?> abstractAssert = makeAssertion(key, value);
    String description = "%s attribute '%s'";
    return abstractAssert.as(description, key.getType(), key.getKey());
  }

  private static AbstractAssert<? extends AbstractAssert<?, ?>, ?> makeAssertion(
      AttributeKey<?> key, @Nullable Object value) {
    switch (key.getType()) {
      case STRING:
        return assertThat((String) value);
      case BOOLEAN:
        return assertThat((Boolean) value);
      case LONG:
        return assertThat((Long) value);
      case DOUBLE:
        return assertThat((Double) value);
      case STRING_ARRAY:
      case BOOLEAN_ARRAY:
      case LONG_ARRAY:
      case DOUBLE_ARRAY:
        return assertThat((List<?>) value);
      case VALUE:
        return assertThat((Value<?>) value);
    }
    throw new IllegalArgumentException("Unknown type for key " + key);
  }

  AttributeAssertion() {}
}
