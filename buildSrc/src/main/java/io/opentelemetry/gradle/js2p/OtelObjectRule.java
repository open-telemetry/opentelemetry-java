/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle.js2p;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import javax.annotation.Nullable;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.ObjectRule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.ParcelableHelper;
import org.jsonschema2pojo.util.ReflectionHelper;

/**
 * An {@link ObjectRule} that replaces jsonschema2pojo's generated {@code toString}/{@code
 * equals}/{@code hashCode} with implementations that mirror AutoValue's style.
 */
public class OtelObjectRule extends ObjectRule {

  public OtelObjectRule(
      RuleFactory ruleFactory,
      ParcelableHelper parcelableHelper,
      ReflectionHelper reflectionHelper) {
    super(ruleFactory, parcelableHelper, reflectionHelper);
  }

  @Override
  public JType apply(
      String nodeName, JsonNode node, JsonNode parent, JPackage pkg, Schema schema) {
    JType type = super.apply(nodeName, node, parent, pkg, schema);
    if (type instanceof JDefinedClass
        && ((JDefinedClass) type).getClassType() == ClassType.CLASS) {
      addValueMethods((JDefinedClass) type);
    }
    return type;
  }

  private static void addValueMethods(JDefinedClass clazz) {
    JCodeModel model = clazz.owner();

    addToString(clazz, model);
    addHashCode(clazz, model);
    addEquals(clazz, model);
  }

  // toString: ClassName{field1=value1, field2=value2}
  private static void addToString(JDefinedClass clazz, JCodeModel model) {
    JMethod toString = clazz.method(JMod.PUBLIC, model.ref(String.class), "toString");
    toString.annotate(Override.class);

    StringBuilder expr = new StringBuilder("return \"").append(clazz.name()).append("{\"");
    boolean first = true;
    for (JFieldVar field : clazz.fields().values()) {
      if (isStatic(field)) {
        continue;
      }
      expr.append(" + \"")
          .append(first ? "" : ", ")
          .append(field.name())
          .append("=\" + ")
          .append(field.name());
      first = false;
    }
    expr.append(" + \"}\";");
    toString.body().directStatement(expr.toString());
  }

  // equals: instanceof + cast + (this.f == null ? that.f == null : this.f.equals(that.f)) && ...
  private static void addEquals(JDefinedClass clazz, JCodeModel model) {
    JMethod equals = clazz.method(JMod.PUBLIC, model.BOOLEAN, "equals");
    equals.annotate(Override.class);
    JVar other = equals.param(model.ref(Object.class), "o");
    other.annotate(Nullable.class);
    JBlock body = equals.body();

    body._if(other.eq(JExpr._this()))._then()._return(JExpr.TRUE);

    JBlock matched = body._if(other._instanceof(clazz))._then();
    matched.directStatement(clazz.name() + " that = (" + clazz.name() + ") o;");

    StringBuilder comparison = new StringBuilder("return ");
    boolean first = true;
    for (JFieldVar field : clazz.fields().values()) {
      if (isStatic(field)) {
        continue;
      }
      String name = field.name();
      comparison
          .append(first ? "" : " && ")
          .append("(this.").append(name).append(" == null ? that.").append(name)
          .append(" == null : this.").append(name).append(".equals(that.").append(name)
          .append("))");
      first = false;
    }
    matched.directStatement(first ? "return true;" : comparison.append(";").toString());

    body._return(JExpr.FALSE);
  }

  // hashCode: h = 1; h *= 1000003; h ^= (f == null ? 0 : f.hashCode()); ...
  private static void addHashCode(JDefinedClass clazz, JCodeModel model) {
    JMethod hashCode = clazz.method(JMod.PUBLIC, model.INT, "hashCode");
    hashCode.annotate(Override.class);
    JBlock body = hashCode.body();
    JVar h = body.decl(model.INT, "h", JExpr.lit(1));

    for (JFieldVar field : clazz.fields().values()) {
      if (isStatic(field)) {
        continue;
      }
      String name = field.name();
      body.directStatement("h *= 1000003;");
      body.directStatement("h ^= (this." + name + " == null) ? 0 : this." + name + ".hashCode();");
    }
    body._return(h);
  }

  private static boolean isStatic(JFieldVar field) {
    return (field.mods().getValue() & JMod.STATIC) == JMod.STATIC;
  }
}
