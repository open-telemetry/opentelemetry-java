/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.AdditionalPropertiesRule;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * An {@link AdditionalPropertiesRule} that drops the generated {@code void setAdditionalProperty}
 * and moves {@code @JsonAnySetter} onto the identical-bodied {@code withAdditionalProperty}
 * builder, mirroring how regular properties expose only a {@code withX} mutator.
 */
public class OtelAdditionalPropertiesRule extends AdditionalPropertiesRule {

  OtelAdditionalPropertiesRule(RuleFactory ruleFactory) {
    super(ruleFactory);
  }

  @Override
  public JDefinedClass apply(
      String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
    JDefinedClass result = super.apply(nodeName, node, parent, jclass, schema);

    // Move @JsonAnySetter from the removed void setter onto the withX builder.
    result.methods().removeIf(method -> method.name().equals("setAdditionalProperty"));
    for (JMethod method : result.methods()) {
      if (method.name().equals("withAdditionalProperty")) {
        method.annotate(JsonAnySetter.class);
        break;
      }
    }
    return result;
  }
}
