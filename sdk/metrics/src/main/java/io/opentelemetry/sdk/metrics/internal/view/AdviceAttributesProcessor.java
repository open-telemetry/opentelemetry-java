/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class AdviceAttributesProcessor extends AttributesProcessor {

  private final Set<AttributeKey<?>> attributeKeys;

  AdviceAttributesProcessor(List<AttributeKey<?>> adviceAttributeKeys) {
    this.attributeKeys = new HashSet<>(adviceAttributeKeys);
  }

  @Override
  public Attributes process(Attributes incoming, Context context) {
    AttributesBuilder builder = incoming.toBuilder();
    builder.removeIf(key -> !attributeKeys.contains(key));
    return builder.build();
  }

  @Override
  public boolean usesContext() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdviceAttributesProcessor that = (AdviceAttributesProcessor) o;
    return attributeKeys.equals(that.attributeKeys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeKeys);
  }

  @Override
  public String toString() {
    return "AdviceAttributesProcessor{" + "attributeKeys=" + attributeKeys + '}';
  }
}
