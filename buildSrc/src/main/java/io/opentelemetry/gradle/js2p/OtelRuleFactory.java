/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.rules.ObjectRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.ParcelableHelper;

/**
 * Custom {@link RuleFactory} that swaps in {@link OtelObjectRule} so generated POJOs get
 * AutoValue-style {@code toString}/{@code equals}/{@code hashCode} implementations instead of
 * jsonschema2pojo's defaults.
 *
 * <p>Referenced from {@code sdk-extensions/declarative-config/build.gradle.kts} via {@code
 * jsonSchema2Pojo.customRuleFactory}.
 */
public class OtelRuleFactory extends RuleFactory {

  @Override
  public Rule<JPackage, JType> getObjectRule() {
    return new OtelObjectRule(this, new ParcelableHelper(), getReflectionHelper());
  }
}
