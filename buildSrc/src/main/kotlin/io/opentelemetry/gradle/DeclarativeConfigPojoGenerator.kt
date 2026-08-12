/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.gradle

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * POJO generator for the declarative-config model package. Reads the opentelemetry-configuration
 * JSON Schema and emits Java source files directly.
 *
 * Design notes:
 * - All schema types (object types and enums) get a "Model" suffix.
 * - All enums are top-level files — not nested inner classes — regardless of how many schema types
 *   reference them. Cross-model references import the enum type and use its simple name.
 * - "Experimental*" types go into the model.internal sub-package.
 * - Properties with object-typed additionalProperties get a synthetic "*PropertyModel" companion
 *   class. When additionalProperties is boolean true (root schema) Map<String, Object> is used
 *   directly.
 * - All $refs are local (#/$defs/...).
 */
class DeclarativeConfigPojoGenerator(
  private val schemaFile: File,
  private val outputDir: File,
  private val modelPackage: String,
) {
  private val internalPackage = "$modelPackage.internal"
  private val mapper = ObjectMapper()
  private lateinit var defs: Map<String, JsonNode>
  private lateinit var enumDefNames: Set<String>
  private lateinit var extensibleDefNames: Set<String>

  // Carries resolved type information for a single property.
  private data class ResolvedType(val expr: String, val imports: Set<String> = emptySet())

  // Carries all derived information about one schema property.
  private data class FieldInfo(
    val jsonKey: String,
    val javaField: String,
    val getter: String,
    val builder: String,
    val typeExpr: String,
    val typeImports: Set<String>,
    val description: String?,
  )

  fun generate() {
    val schema = mapper.readTree(schemaFile)
    defs = schema["\$defs"]?.properties()?.associate { it.key to it.value } ?: emptyMap()

    enumDefNames = defs.entries.filter { (_, node) -> node.has("enum") }.map { it.key }.toSet()
    extensibleDefNames = defs.entries
      .filter { (_, node) -> node["additionalProperties"]?.let { !it.isBoolean && it.isObject } == true }
      .map { it.key }
      .toSet()

    // Generate root schema as OpenTelemetryConfigurationModel.
    val rootTitle = schema["title"]?.asText() ?: error("Root schema has no title")
    generateClassFile(rootTitle, schema)

    for ((defName, defNode) in defs) {
      if (defNode.has("enum")) generateEnumFile(defName, defNode)
      else generateClassFile(defName, defNode)
    }
  }

  // ── Naming ──────────────────────────────────────────────────────────────────

  private fun isExperimental(schemaKey: String) = schemaKey.startsWith("Experimental")

  private fun packageForKey(schemaKey: String) =
    if (isExperimental(schemaKey)) internalPackage else modelPackage

  private fun classNameForKey(key: String) = "${key}Model"

  private fun javaTypeForRef(defKey: String) = classNameForKey(defKey)

  // ── Property name conversion ─────────────────────────────────────────────────

  /**
   * Maps a JSON property name (snake_case, may contain "/") to a Java field name.
   * "default" → "_default" (reserved keyword); "foo_bar/baz" → "fooBarBaz".
   */
  private fun fieldName(jsonKey: String): String {
    val raw = jsonKey
      .split("_", "/")
      .mapIndexed { i, part -> if (i == 0) part else part.replaceFirstChar { it.uppercase() } }
      .joinToString("")
    return if (raw == "default") "_default" else raw
  }

  /**
   * The base name used to build getter/builder names. Strips the leading underscore from
   * "_default" so the methods are "getDefault"/"withDefault", not "get_default".
   */
  private fun baseNameFor(jsonKey: String): String {
    val field = fieldName(jsonKey)
    return if (field == "_default") "default" else field
  }

  private fun getterName(jsonKey: String) =
    "get${baseNameFor(jsonKey).replaceFirstChar { it.uppercase() }}"

  private fun builderName(jsonKey: String) =
    "with${baseNameFor(jsonKey).replaceFirstChar { it.uppercase() }}"

  // ── Type resolution ──────────────────────────────────────────────────────────

  private fun resolvePropertyType(propNode: JsonNode, fromPackage: String): ResolvedType {
    val ref = propNode["\$ref"]?.asText()
    if (ref != null) return resolveDefRef(ref.removePrefix("#/\$defs/"), fromPackage)

    if (isArrayType(propNode["type"])) return resolveArrayType(propNode["items"], fromPackage)

    if (propNode.has("oneOf")) return ResolvedType("Object")

    for (kw in listOf("anyOf", "allOf", "if")) {
      if (propNode.has(kw)) error(
        "Unhandled schema keyword \"$kw\" encountered during generation. " +
          "Update the generator to handle this construct. Property node: $propNode"
      )
    }

    // Enums are only handled when declared as top-level \$defs (see enumDefNames/generateEnumFile);
    // a \$ref to such a def resolves to its generated *Model enum above. An inline enum here would
    // otherwise fall through to resolveScalarType and be silently emitted as its base scalar type
    // (e.g. String), losing type safety.
    if (propNode.has("enum")) error(
      "Inline enum encountered during generation. Promote it to a top-level \$defs entry " +
        "so it generates a typed *Model enum, or update the generator to handle inline enums. " +
        "Property node: $propNode"
    )

    return resolveScalarType(propNode["type"])
  }

  private fun resolveDefRef(defKey: String, fromPackage: String): ResolvedType {
    val defPkg = packageForKey(defKey)
    val simple = javaTypeForRef(defKey)
    return if (defPkg == fromPackage) ResolvedType(simple)
    else ResolvedType(simple, setOf("$defPkg.$simple"))
  }

  private fun resolveArrayType(itemsNode: JsonNode?, fromPackage: String): ResolvedType {
    if (itemsNode == null) error(
      "Array property has no \"items\" schema: cannot determine element type. " +
        "Update the generator to handle this construct."
    )

    val ref = itemsNode["\$ref"]?.asText()
    if (ref != null) {
      val inner = resolveDefRef(ref.removePrefix("#/\$defs/"), fromPackage)
      return ResolvedType("List<${inner.expr}>", inner.imports + "java.util.List")
    }

    val javaType = when (itemsNode["type"]?.asText()) {
      "string" -> "String"
      "number" -> "Double"
      "integer" -> "Integer"
      "boolean" -> "Boolean"
      else -> error(
        "Unhandled array items type \"${itemsNode["type"]?.asText()}\" encountered during generation. " +
          "Update the generator to handle this type. Items node: $itemsNode"
      )
    }
    return ResolvedType("List<$javaType>", setOf("java.util.List"))
  }

  private fun resolveScalarType(typeNode: JsonNode?): ResolvedType {
    if (typeNode == null) error(
      "Property has no \$ref, type, oneOf, anyOf, allOf, or if: cannot determine Java type. " +
        "Update the generator to handle this construct."
    )
    val types = if (typeNode.isArray) typeNode.map { it.asText() } else listOf(typeNode.asText())
    return when (types.firstOrNull { it != "null" }) {
      "string" -> ResolvedType("String")
      "integer" -> ResolvedType("Integer")
      "boolean" -> ResolvedType("Boolean")
      "number" -> ResolvedType("Double")
      else -> error(
        "Unhandled scalar type(s) $types encountered during generation. " +
          "Update the generator to handle this type."
      )
    }
  }

  /** Returns true if the type node represents an array (scalar or array-of-types form). */
  private fun isArrayType(typeNode: JsonNode?): Boolean {
    if (typeNode == null) return false
    return typeNode.asText() == "array" || (typeNode.isArray && typeNode.any { it.asText() == "array" })
  }

  // ── File generation ──────────────────────────────────────────────────────────

  private fun generateEnumFile(defKey: String, defNode: JsonNode) {
    val className = classNameForKey(defKey)
    val pkg = packageForKey(defKey)
    val values = defNode["enum"].map { it.asText() }
    writeFile(pkg, className, buildEnumSource(pkg, className, values))
  }

  private fun generateClassFile(defKey: String, defNode: JsonNode) {
    val className = classNameForKey(defKey)
    val pkg = packageForKey(defKey)
    val isExtensible = defKey in extensibleDefNames
    val hasOpenAdditionalProperties = defNode["additionalProperties"]?.isBoolean == true &&
      defNode["additionalProperties"].asBoolean()

    writeFile(pkg, className, buildClassSource(pkg, className, defKey, defNode, isExtensible, hasOpenAdditionalProperties))

    if (isExtensible) {
      val propClassName = "${defKey}PropertyModel"
      writeFile(pkg, propClassName, buildPropertyModelSource(pkg, propClassName))
    }
  }

  private fun writeFile(pkg: String, className: String, content: String) {
    val pkgDir = outputDir.resolve(pkg.replace('.', '/'))
    pkgDir.mkdirs()
    pkgDir.resolve("$className.java").writeText(content)
  }

  // ── Source builders ──────────────────────────────────────────────────────────

  private fun buildEnumSource(pkg: String, className: String, values: List<String>) = buildString {
    val constants = values.map { it.toScreamingSnakeCase() to it }

    appendLicense()
    append("\npackage $pkg;\n\n")
    append("import com.fasterxml.jackson.annotation.JsonCreator;\n")
    append("import com.fasterxml.jackson.annotation.JsonValue;\n")
    append("import java.util.HashMap;\n")
    append("import java.util.Map;\n")
    append("import javax.annotation.Generated;\n")
    append("\n@Generated(\"io.opentelemetry.gradle.DeclarativeConfigPojoGenerator\")\n")
    append("public enum $className {\n")

    constants.forEachIndexed { i, (name, value) ->
      val terminator = if (i < constants.size - 1) "," else ";"
      append("  $name(\"$value\")$terminator\n")
    }

    append("  private final String value;\n")
    append("  private static final Map<String, $className> CONSTANTS = new HashMap<String, $className>();\n\n")
    append("  static {\n    for ($className c : values()) {\n      CONSTANTS.put(c.value, c);\n    }\n  }\n\n")
    append("  $className(String value) {\n    this.value = value;\n  }\n\n")
    append("  @Override\n  public String toString() {\n    return this.value;\n  }\n\n")
    append("  @JsonValue\n  public String value() {\n    return this.value;\n  }\n\n")
    append("  @JsonCreator\n  public static $className fromValue(String value) {\n")
    append("    $className constant = CONSTANTS.get(value);\n")
    append("    if (constant == null) {\n      throw new IllegalArgumentException(value);\n")
    append("    } else {\n      return constant;\n    }\n  }\n}\n")
  }

  private fun buildClassSource(
    pkg: String,
    className: String,
    defKey: String,
    defNode: JsonNode,
    isExtensible: Boolean,
    hasOpenAdditionalProperties: Boolean,
  ) = buildString {
    val properties = defNode["properties"]?.properties()?.toList() ?: emptyList()

    val fields = properties.map { (jsonKey, propNode) ->
      val resolved = resolvePropertyType(propNode, pkg)
      FieldInfo(
        jsonKey = jsonKey,
        javaField = fieldName(jsonKey),
        getter = getterName(jsonKey),
        builder = builderName(jsonKey),
        typeExpr = resolved.expr,
        typeImports = resolved.imports,
        description = propNode["description"]?.asText(),
      )
    }

    val companionName = if (isExtensible) "${defKey}PropertyModel" else null
    val additionalPropsType = when {
      isExtensible -> "Map<String, $companionName>"
      hasOpenAdditionalProperties -> "Map<String, Object>"
      else -> null
    }

    val imports = sortedSetOf(
      "com.fasterxml.jackson.annotation.JsonInclude",
      "com.fasterxml.jackson.annotation.JsonPropertyOrder",
      "javax.annotation.Generated",
      "javax.annotation.Nullable",
    )
    if (fields.any { it.typeExpr.startsWith("List<") }) imports.add("java.util.List")
    if (additionalPropsType != null) {
      imports.addAll(listOf(
        "java.util.LinkedHashMap",
        "java.util.Map",
        "com.fasterxml.jackson.annotation.JsonAnyGetter",
        "com.fasterxml.jackson.annotation.JsonAnySetter",
      ))
    }
    if (fields.isNotEmpty()) imports.add("com.fasterxml.jackson.annotation.JsonProperty")
    for (f in fields) imports.addAll(f.typeImports)

    appendLicense()
    append("\npackage $pkg;\n\n")
    for (imp in imports) append("import $imp;\n")
    append("\n@JsonInclude(JsonInclude.Include.NON_NULL)\n")

    val propKeys = properties.map { "\"${it.key}\"" }
    if (propKeys.isEmpty()) {
      append("@JsonPropertyOrder({})\n")
    } else {
      append("@JsonPropertyOrder({\n")
      propKeys.forEachIndexed { i, k ->
        append("  $k${if (i < propKeys.size - 1) "," else ""}\n")
      }
      append("})\n")
    }

    append("@Generated(\"io.opentelemetry.gradle.DeclarativeConfigPojoGenerator\")\n")
    append("public class $className {\n\n")

    // Fields
    for (f in fields) append("  @Nullable private ${f.typeExpr} ${f.javaField};\n")
    if (additionalPropsType != null) {
      val init = if (isExtensible) "new LinkedHashMap<String, $companionName>()"
      else "new LinkedHashMap<String, Object>()"
      append("  private $additionalPropsType additionalProperties = $init;\n")
    }
    if (fields.isNotEmpty() || additionalPropsType != null) append("\n")

    // Getters and builders (all fields are nullable)
    for (f in fields) {
      if (f.description != null) {
        append("  /**\n")
        for (line in formatJavadoc(f.description)) append("   * $line\n")
        append("   */\n")
      }
      append("  @JsonProperty(\"${f.jsonKey}\")\n")
      append("  @Nullable\n")
      append("  public ${f.typeExpr} ${f.getter}() {\n    return ${f.javaField};\n  }\n\n")
      append("  @JsonProperty(\"${f.jsonKey}\")\n")
      append("  public $className ${f.builder}(${f.typeExpr} ${f.javaField}) {\n")
      append("    this.${f.javaField} = ${f.javaField};\n    return this;\n  }\n\n")
    }

    // additionalProperties getter/builder
    if (additionalPropsType != null) {
      val apValueType = companionName ?: "Object"
      append("  @JsonAnyGetter\n")
      append("  public $additionalPropsType getAdditionalProperties() {\n    return this.additionalProperties;\n  }\n\n")
      append("  @JsonAnySetter\n")
      append("  public $className withAdditionalProperty(String name, $apValueType value) {\n")
      append("    this.additionalProperties.put(name, value);\n    return this;\n  }\n\n")
    }

    val allFields = fields.map { it.javaField } +
      (if (additionalPropsType != null) listOf("additionalProperties") else emptyList())

    appendToString(className, allFields)
    append("\n")
    appendHashCode(allFields)
    append("\n")
    appendEquals(className, allFields)
    append("}\n")
  }

  private fun buildPropertyModelSource(pkg: String, className: String) = buildString {
    appendLicense()
    append("\npackage $pkg;\n\n")
    append("import com.fasterxml.jackson.annotation.JsonAnyGetter;\n")
    append("import com.fasterxml.jackson.annotation.JsonAnySetter;\n")
    append("import com.fasterxml.jackson.annotation.JsonInclude;\n")
    append("import com.fasterxml.jackson.annotation.JsonPropertyOrder;\n")
    append("import java.util.LinkedHashMap;\n")
    append("import java.util.Map;\n")
    append("import javax.annotation.Generated;\n")
    append("import javax.annotation.Nullable;\n")
    append("\n@JsonInclude(JsonInclude.Include.NON_NULL)\n")
    append("@JsonPropertyOrder({})\n")
    append("@Generated(\"io.opentelemetry.gradle.DeclarativeConfigPojoGenerator\")\n")
    append("public class $className {\n\n")
    append("  private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();\n\n")
    append("  @JsonAnyGetter\n")
    append("  public Map<String, Object> getAdditionalProperties() {\n    return this.additionalProperties;\n  }\n\n")
    append("  @JsonAnySetter\n")
    append("  public $className withAdditionalProperty(String name, Object value) {\n")
    append("    this.additionalProperties.put(name, value);\n    return this;\n  }\n\n")
    appendToString(className, listOf("additionalProperties"))
    append("\n")
    appendHashCode(listOf("additionalProperties"))
    append("\n")
    appendEquals(className, listOf("additionalProperties"))
    append("}\n")
  }

  // ── Shared method emitters ───────────────────────────────────────────────────

  private fun StringBuilder.appendToString(className: String, fields: List<String>) {
    append("  @Override\n  public String toString() {\n")
    if (fields.isEmpty()) {
      append("    return \"$className{}\";\n")
    } else {
      val parts = fields.mapIndexed { i, name ->
        if (i == 0) "\"$name=\" + $name" else "\", $name=\" + $name"
      }
      append("    return \"$className{\" + ${parts.joinToString(" + ")} + \"}\";\n")
    }
    append("  }\n")
  }

  private fun StringBuilder.appendHashCode(fields: List<String>) {
    append("  @Override\n  public int hashCode() {\n    int h = 1;\n")
    for (name in fields) {
      append("    h *= 1000003;\n")
      append("    h ^= (this.$name == null) ? 0 : this.$name.hashCode();\n")
    }
    append("    return h;\n  }\n")
  }

  private fun StringBuilder.appendEquals(className: String, fields: List<String>) {
    append("  @Override\n  public boolean equals(@Nullable Object o) {\n")
    append("    if (o == this) {\n      return true;\n    }\n")
    append("    if (o instanceof $className) {\n      $className that = ($className) o;\n")
    if (fields.isEmpty()) {
      append("      return true;\n")
    } else {
      val conditions = fields.map { "(this.$it == null ? that.$it == null : this.$it.equals(that.$it))" }
      append("      return ${conditions.joinToString("\n          && ")};\n")
    }
    append("    }\n    return false;\n  }\n")
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  /**
   * Converts a JSON enum value to a Java SCREAMING_SNAKE_CASE constant name.
   * Splits on "_", "/", "-", and letter-digit boundaries.
   * e.g. "trace2" → "TRACE_2", "no_utf8_escaping/development" → "NO_UTF_8_ESCAPING_DEVELOPMENT"
   */
  private fun String.toScreamingSnakeCase() = this
    .replace("/", "_")
    .replace("-", "_")
    .replace(Regex("([a-zA-Z])(\\d)"), "$1_$2")
    .replace(Regex("(\\d)([a-zA-Z])"), "$1_$2")
    .split("_")
    .joinToString("_") { it.uppercase() }

  /**
   * Formats a schema description string into javadoc lines. Each newline in the schema becomes a
   * paragraph break so google-java-format doesn't collapse them into one line.
   */
  private fun formatJavadoc(description: String): List<String> {
    val lines = description.trimEnd('\n').split('\n')
    if (lines.size == 1) return lines
    return buildList {
      lines.forEachIndexed { i, line ->
        when {
          i == 0 -> add(line)
          // Empty lines arise from \n\n paragraph breaks in the schema. Skip them: the next
          // non-empty line will open its own <p>, so the empty string itself gets no tag.
          line.isEmpty() -> Unit
          else -> { add(""); add("<p>$line") }
        }
      }
    }
  }

  private fun StringBuilder.appendLicense() {
    append("/*\n * Copyright The OpenTelemetry Authors\n * SPDX-License-Identifier: Apache-2.0\n */\n")
  }
}
