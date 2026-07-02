/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import javax.annotation.Nullable;
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * Annotates every generated property field and getter with {@code @Nullable}.
 *
 * <p>jsonschema2pojo's built-in JSR-305 support ({@code includeJsr305Annotations}) annotates
 * required fields with {@code @Nonnull} and optional fields with {@code @Nullable}. The {@code
 * @Nonnull} fields are never initialized (Jackson populates them reflectively), which makes NullAway
 * flag them as uninitialized {@code @NonNull} fields. Since the generated getters are uniformly
 * {@code @Nullable} anyway and field presence is validated at runtime by the model factories, we
 * disable {@code includeJsr305Annotations} and instead annotate everything {@code @Nullable} here.
 */
public class NullableAnnotator extends AbstractAnnotator {

  @Override
  public void propertyField(
      JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
    field.annotate(Nullable.class);
  }

  @Override
  public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
    getter.annotate(Nullable.class);
  }

  @Override
  public boolean isPolymorphicDeserializationSupported(JsonNode node) {
    // Defer to the composed Jackson annotator rather than vetoing polymorphic deserialization.
    return true;
  }
}
