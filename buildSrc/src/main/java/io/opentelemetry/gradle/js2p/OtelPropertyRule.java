/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.PropertyRule;
import org.jsonschema2pojo.rules.RuleFactory;

/**
 * A {@link PropertyRule} that emits each property's {@code description} only on its getter, and
 * annotates the generated {@code withX} builder method with {@code @JsonProperty}.
 *
 * <p>By default jsonschema2pojo writes a property description in three places: the field javadoc,
 * the getter javadoc, and a {@code @JsonPropertyDescription} annotation on the field. The getter is
 * the public API surface users discover, so we consolidate the description there and drop the field
 * javadoc and annotation.
 *
 * <p>This also fixes a jsonschema2pojo limitation: it follows draft-03/04 semantics where a {@code
 * $ref} replaces its sibling keywords, so a {@code description} written as a sibling of {@code $ref}
 * is dropped entirely. The configuration schema is draft 2020-12, where {@code $ref} siblings are
 * valid; re-applying from the original node restores those descriptions on the getter too.
 *
 * <p>Mechanism: strip {@code description} from the node before delegating to {@link
 * PropertyRule#apply} so the superclass does not add the property description to the field or
 * getter. The superclass calls {@code resolveRefs()} before applying annotations, so if the
 * property node is a {@code $ref} whose target {@code $def} has its own description, that
 * description would otherwise land on the field and getter via {@link
 * org.jsonschema2pojo.rules.DescriptionRule}. {@link OtelDescriptionRule} suppresses all
 * description placements on fields and getters from that path. The property's own description
 * (stripped from the node before delegation) is then applied to the getter only by
 * {@link #applyGetterDescription}, which writes directly to the getter's javadoc.
 *
 * <p>{@link OtelJacksonAnnotator} puts {@code @JsonProperty} on the getter, but has no hook for the
 * {@code withX} builder methods, so this rule annotates them.
 */
public class OtelPropertyRule extends PropertyRule {

  private final RuleFactory ruleFactory;

  OtelPropertyRule(RuleFactory ruleFactory) {
    super(ruleFactory);
    this.ruleFactory = ruleFactory;
  }

  @Override
  public JDefinedClass apply(
      String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
    JsonNode description = node.get("description");

    // Delegate with the description removed so the superclass places it nowhere; we re-apply it to
    // the getter below.
    JsonNode delegateNode = node;
    if (description != null && node.isObject()) {
      delegateNode = node.deepCopy();
      ((ObjectNode) delegateNode).remove("description");
    }

    JDefinedClass result = super.apply(nodeName, delegateNode, parent, jclass, schema);

    String propertyName = ruleFactory.getNameHelper().getPropertyName(nodeName, node);
    JFieldVar field = result.fields().get(propertyName);
    if (field == null) {
      return result;
    }

    annotateBuilderMethod(result, propertyName, node, nodeName);

    if (description != null) {
      applyGetterDescription(nodeName, node, schema, result, propertyName, field, description);
    }
    return result;
  }

  /** Binds Jackson to withX so deserialization goes through the builder, not the private field. */
  private void annotateBuilderMethod(
      JDefinedClass jclass, String propertyName, JsonNode node, String jsonName) {
    String builderName = ruleFactory.getNameHelper().getBuilderName(propertyName, node);
    for (JMethod method : jclass.methods()) {
      if (method.name().equals(builderName)) {
        method.annotate(JsonProperty.class).param("value", jsonName);
        return;
      }
    }
  }

  private void applyGetterDescription(
      String nodeName,
      JsonNode node,
      Schema schema,
      JDefinedClass jclass,
      String propertyName,
      JFieldVar field,
      JsonNode description) {
    String getterName = ruleFactory.getNameHelper().getGetterName(propertyName, field.type(), node);
    String text = preserveLineBreaks(description).asText();
    for (JMethod method : jclass.methods()) {
      if (method.name().equals(getterName)) {
        // OtelDescriptionRule suppresses DescriptionRule for JMethod targets, so write the
        // description directly. JCommentPart.format() handles embedded '\n' characters by
        // emitting line breaks, so append the whole text as one string to preserve newlines.
        method.javadoc().append(text);
        return;
      }
    }
  }

  // google-java-format reflows javadoc prose, collapsing the schema's single newlines into spaces
  // (only blank lines survive, rendered as <p>). Promote each interior lone newline to a blank line
  // so every original line becomes its own <p> paragraph. Existing blank lines are left alone, as is
  // a trailing newline (so the description doesn't gain a stray trailing blank paragraph).
  private static JsonNode preserveLineBreaks(JsonNode description) {
    String text = description.asText();
    if (text.indexOf('\n') < 0) {
      return description;
    }
    boolean trailingNewline = text.endsWith("\n");
    String body = trailingNewline ? text.substring(0, text.length() - 1) : text;
    body = body.replaceAll("(?<!\n)\n(?!\n)", "\n\n");
    return TextNode.valueOf(trailingNewline ? body + "\n" : body);
  }
}
