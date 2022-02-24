/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/** An assertion on an attribute key. */
@AutoValue
public abstract class AttributeAssertion {

  // This method is not type-safe! But because the constructor is private, we know it will only be
  // created through
  // our factories, which are type-safe.
  @SuppressWarnings("unchecked")
  static AttributeAssertion create(
      AttributeKey<?> key, Consumer<? extends AbstractAssert<?, ?>> assertion) {
    return new AutoValue_AttributeAssertion(key, (Consumer<AbstractAssert<?, ?>>) assertion);
  }

  abstract AttributeKey<?> getKey();

  abstract Consumer<AbstractAssert<?, ?>> getAssertion();

  AttributeAssertion() {}
}
