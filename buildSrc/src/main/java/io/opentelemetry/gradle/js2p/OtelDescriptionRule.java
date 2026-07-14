/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.DescriptionRule;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A {@link DescriptionRule} that suppresses javadoc placement on fields and getters.
 *
 * <p>jsonschema2pojo's {@code PropertyRule.apply()} calls {@code resolveRefs()} before applying
 * annotations. When a property node is {@code {"$ref": "..."}}, the resolved node is the
 * referenced {@code $def}'s content. If that {@code $def} has a {@code description}, jsonschema2pojo
 * applies it to both the field and the getter via this rule.
 *
 * <p>This rule skips {@link JFieldVar} targets entirely (fields carry no javadoc) and skips
 * {@link JMethod} targets (descriptions on getters are applied explicitly by
 * {@link OtelPropertyRule}, using the property's own {@code description}, not the {@code $def}'s).
 * Class-level targets ({@link com.sun.codemodel.JDefinedClass}) are passed through unchanged.
 */
class OtelDescriptionRule extends DescriptionRule {

  @Override
  public JDocComment apply(
      String nodeName,
      JsonNode node,
      JsonNode parent,
      JDocCommentable generatableType,
      Schema schema) {
    if (generatableType instanceof JFieldVar || generatableType instanceof JMethod) {
      // Do not call generatableType.javadoc() here: invoking javadoc() on a target that has
      // no existing JDocComment auto-creates an empty one, which causes JFieldVar.declare() to
      // emit an empty /** */ block in the generated source.
      return null;
    }
    return super.apply(nodeName, node, parent, generatableType, schema);
  }
}
