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
import org.jsonschema2pojo.exception.GenerationException;
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
 * PropertyRule#apply} so the superclass adds it nowhere (not the field javadoc, the getter javadoc,
 * or a {@code @JsonPropertyDescription}), then apply it to the getter only — leaving fields without
 * any javadoc.
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

    // A $ref target with its own top-level description would land on the field via DescriptionRule
    // and conflict with the sibling description we re-apply to the getter. The schema puts
    // descriptions on properties, not $defs, so this doesn't happen today; fail loud if it does.
    if (referencedTypeHasDescription(node, schema)) {
      throw new GenerationException(
          "Property '"
              + nodeName
              + "' resolves to a type that defines its own top-level description, which is not"
              + " handled (it would land on the field). See "
              + OtelPropertyRule.class.getName()
              + ".");
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

  /**
   * Returns whether a (possibly chained) {@code $ref} resolves to a schema with its own
   * description. Uses {@link org.jsonschema2pojo.SchemaStore} rather than inspecting {@code
   * field.javadoc()} (which would instantiate an empty comment).
   */
  private boolean referencedTypeHasDescription(JsonNode node, Schema schema) {
    JsonNode current = node;
    Schema currentSchema = schema;
    boolean resolved = false;
    while (current.has("$ref")) {
      currentSchema =
          ruleFactory
              .getSchemaStore()
              .create(
                  currentSchema,
                  current.get("$ref").asText(),
                  ruleFactory.getGenerationConfig().getRefFragmentPathDelimiters());
      current = currentSchema.getContent();
      resolved = true;
    }
    return resolved && current.has("description");
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
    for (JMethod method : jclass.methods()) {
      if (method.name().equals(getterName)) {
        ruleFactory
            .getDescriptionRule()
            .apply(nodeName, preserveLineBreaks(description), node, method, schema);
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
