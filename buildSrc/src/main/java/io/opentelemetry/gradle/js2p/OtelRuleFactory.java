/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
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
 *       instead of jsonschema2pojo's defaults, and routes experimental classes into the {@code
 *       internal} sub-package.
 *   <li>{@link OtelEnumRule} — routes top-level experimental enums into the {@code internal}
 *       sub-package, mirroring {@link OtelObjectRule} for classes.
 *   <li>{@link OtelPropertyRule} — emits property descriptions on getters only (restoring those
 *       written as {@code $ref} siblings, which jsonschema2pojo drops) and annotates the {@code
 *       withX} builder methods with {@code @JsonProperty}.
 *   <li>{@link OtelAdditionalPropertiesRule} — removes the redundant {@code setAdditionalProperty},
 *       moving {@code @JsonAnySetter} onto {@code withAdditionalProperty}.
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
  public Rule<JClassContainer, JType> getEnumRule() {
    return new OtelEnumRule(this);
  }

  @Override
  public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
    return new OtelPropertyRule(this);
  }

  @Override
  public Rule<JDocCommentable, JDocComment> getDescriptionRule() {
    return new OtelDescriptionRule();
  }

  @Override
  public Rule<JDefinedClass, JDefinedClass> getAdditionalPropertiesRule() {
    return new OtelAdditionalPropertiesRule(this);
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
