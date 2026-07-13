/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import java.util.Iterator;
import javax.annotation.Nullable;
import org.jsonschema2pojo.AbstractAnnotator;

/**
 * Replaces jsonschema2pojo's built-in Jackson annotator (disabled via {@code annotationStyle =
 * none}). Puts {@code @JsonProperty} on the getter — and, via {@link OtelPropertyRule}, on the
 * {@code withX} builder — rather than on the private field, so Jackson binds through the builder
 * instead of reflecting on the field. Fields carry only {@code @Nullable} for NullAway.
 *
 * <p>Only reproduces the {@code Jackson2Annotator} hooks these models use: no {@code
 * @JsonDeserialize}, {@code @JsonFormat}, or {@code @JsonTypeInfo} (no {@code Set}/date-time fields
 * or polymorphism).
 */
public class OtelJacksonAnnotator extends AbstractAnnotator {

  @Override
  public void propertyOrder(JDefinedClass clazz, JsonNode propertiesNode) {
    JAnnotationArrayMember value = clazz.annotate(JsonPropertyOrder.class).paramArray("value");
    for (Iterator<String> names = propertiesNode.fieldNames(); names.hasNext(); ) {
      value.param(names.next());
    }
  }

  @Override
  public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
    clazz.annotate(JsonInclude.class).param("value", JsonInclude.Include.NON_NULL);
  }

  @Override
  public void propertyField(
      JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
    // No @JsonProperty on the field; deserialization binds to the annotated withX (see
    // OtelPropertyRule).
    field.annotate(Nullable.class);
  }

  @Override
  public void propertyGetter(JMethod getter, JDefinedClass clazz, String propertyName) {
    getter.annotate(JsonProperty.class).param("value", propertyName);
    getter.annotate(Nullable.class);
  }

  @Override
  public void anyGetter(JMethod getter, JDefinedClass clazz) {
    getter.annotate(JsonAnyGetter.class);
  }

  @Override
  public void anySetter(JMethod setter, JDefinedClass clazz) {
    setter.annotate(JsonAnySetter.class);
  }

  @Override
  public void enumCreatorMethod(JDefinedClass enumClass, JMethod creatorMethod) {
    creatorMethod.annotate(JsonCreator.class);
  }

  @Override
  public void enumValueMethod(JDefinedClass enumClass, JMethod valueMethod) {
    valueMethod.annotate(JsonValue.class);
  }
}
