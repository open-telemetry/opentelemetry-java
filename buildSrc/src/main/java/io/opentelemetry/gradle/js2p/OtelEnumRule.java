/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.EnumRule;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * An {@link EnumRule} that routes top-level experimental enums into the {@code internal}
 * sub-package, mirroring {@link OtelObjectRule} for classes.
 *
 * <p>Only top-level enums (where the container is a {@link JPackage}) are routed. Nested enums are
 * created inside their owning class and move with it automatically.
 *
 * <p>Referenced from {@link OtelRuleFactory#getEnumRule()}.
 */
public class OtelEnumRule extends EnumRule {

  private final RuleFactory ruleFactory;

  public OtelEnumRule(RuleFactory ruleFactory) {
    super(ruleFactory);
    this.ruleFactory = ruleFactory;
  }

  @Override
  public JType apply(
      String nodeName, JsonNode node, JsonNode parent, JClassContainer container, Schema schema) {
    if (container instanceof JPackage) {
      JPackage targetPackage =
          ExperimentalPackages.resolve(ruleFactory, nodeName, node, (JPackage) container);
      return super.apply(nodeName, node, parent, targetPackage, schema);
    }
    return super.apply(nodeName, node, parent, container, schema);
  }
}
