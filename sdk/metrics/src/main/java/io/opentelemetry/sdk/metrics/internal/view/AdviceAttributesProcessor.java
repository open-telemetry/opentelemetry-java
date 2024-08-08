/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AdviceAttributesProcessor extends AttributesProcessor {

  private final Set<AttributeKey<?>> attributeKeys;

  AdviceAttributesProcessor(List<AttributeKey<?>> adviceAttributeKeys) {
    this.attributeKeys = new HashSet<>(adviceAttributeKeys);
  }

  @Override
  public Attributes process(Attributes incoming, Context context) {
    return FilteredAttributes.create(incoming, attributeKeys);
  }

  @Override
  public boolean usesContext() {
    return false;
  }

  @Override
  public String toString() {
    return "AdviceAttributesProcessor{attributeKeys=" + attributeKeys + '}';
  }
}
