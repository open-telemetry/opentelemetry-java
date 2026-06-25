/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.rules.ObjectRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.ParcelableHelper;

/**
 * Custom {@link RuleFactory} for the declarative-config POJO generation. It swaps in:
 *
 * <ul>
 *   <li>{@link OtelObjectRule} — AutoValue-style {@code toString}/{@code equals}/{@code hashCode}
 *       instead of jsonschema2pojo's defaults.
 *   <li>{@link OtelPropertyRule} — restores property descriptions written as siblings of {@code
 *       $ref}, which jsonschema2pojo otherwise drops.
 * </ul>
 *
 * <p>Referenced from {@code sdk-extensions/declarative-config/build.gradle.kts} via {@code
 * jsonSchema2Pojo.customRuleFactory}.
 */
public class OtelRuleFactory extends RuleFactory {

  @Override
  public Rule<JPackage, JType> getObjectRule() {
    return new OtelObjectRule(this, new ParcelableHelper(), getReflectionHelper());
  }

  @Override
  public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
    return new OtelPropertyRule(this);
  }

  // The opentelemetry-configuration schema already documents required-ness in each required
  // property's description ("Property is required and must be non-null."), so jsonschema2pojo's
  // "(Required)" javadoc is purely duplicative. No-op the required rules to drop it. With JSR-303
  // and JSR-305 annotations disabled, the "(Required)" javadoc is these rules' only effect.
  @Override
  public Rule<JDefinedClass, JDefinedClass> getRequiredArrayRule() {
    return (nodeName, node, parent, generatableType, schema) -> generatableType;
  }

  @Override
  public Rule<JDocCommentable, JDocCommentable> getRequiredRule() {
    return (nodeName, node, parent, generatableType, schema) -> generatableType;
  }
}
