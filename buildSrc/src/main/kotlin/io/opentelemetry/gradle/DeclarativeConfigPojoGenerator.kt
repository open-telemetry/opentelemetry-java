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
 * - Properties whose JSON key ends with "/development" are experimental. On stable (non-
 *   Experimental) classes, these properties are not exposed as public getters or builders.
 *   Instead, their deserialized values are stored in an opaque experimentalProperties map, and
 *   a generated "*ModelAccessor" class in model.internal provides typed access.
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

  private data class ResolvedType(val expr: String, val imports: Set<String> = emptySet())

  private data class FieldInfo(
    val jsonKey: String,
    val constName: String, // SCREAMING_SNAKE_CASE version of jsonKey, used as a Java string constant
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

    // The root schema (title: OpenTelemetryConfiguration) generates OpenTelemetryConfigurationModel.
    val rootTitle = schema["title"]?.asText() ?: error("Root schema has no title")
    generateClassFile(rootTitle, schema)

    for ((defName, defNode) in defs) {
      if (defNode.has("enum")) generateEnumFile(defName, defNode)
      else generateClassFile(defName, defNode)
    }
  }

  // ── Naming ──────────────────────────────────────────────────────────────────

  private fun isExperimental(schemaKey: String) = schemaKey.startsWith("Experimental")

  private fun isExperimentalProperty(jsonKey: String) = jsonKey.endsWith("/development")

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

    // Enums are only handled when declared as top-level $defs (see enumDefNames/generateEnumFile);
    // a $ref to such a def resolves to its generated *Model enum above. An inline enum here would
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

  // Handles both "type": "array" and "type": ["array", "null"].
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
    val isStable = !isExperimental(defKey)

    writeFile(pkg, className, buildClassSource(pkg, className, defKey, defNode, isExtensible, hasOpenAdditionalProperties))

    if (isExtensible && !isStable) {
      val propClassName = "${defKey}PropertyModel"
      writeFile(pkg, propClassName, buildPropertyModelSource(pkg, propClassName))
    }

    if (isStable) {
      val experimentalPropPairs = defNode["properties"]?.properties()
        ?.filter { isExperimentalProperty(it.key) }
        ?.toList() ?: emptyList()
      if (experimentalPropPairs.isNotEmpty()) {
        generateExperimentalAccessorFile(defKey, pkg, experimentalPropPairs)
      }
    }
  }

  private fun generateExperimentalAccessorFile(
    defKey: String,
    stablePkg: String,
    experimentalPropPairs: List<Map.Entry<String, JsonNode>>,
  ) {
    val accessorClassName = "${classNameForKey(defKey)}Accessor"
    writeFile(internalPackage, accessorClassName,
      buildExperimentalAccessorSource(defKey, stablePkg, experimentalPropPairs))
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
    val allProperties = defNode["properties"]?.properties()?.toList() ?: emptyList()
    val isStable = !isExperimental(defKey)
    val hasAnyAdditional = isExtensible || hasOpenAdditionalProperties

    val stablePropertyPairs = if (isStable) allProperties.filter { !isExperimentalProperty(it.key) } else allProperties
    val experimentalPropPairs = if (isStable) allProperties.filter { isExperimentalProperty(it.key) } else emptyList<Map.Entry<String, JsonNode>>()

    val fields = stablePropertyPairs.map { (jsonKey, propNode) ->
      val resolved = resolvePropertyType(propNode, pkg)
      FieldInfo(
        jsonKey = jsonKey,
        constName = jsonKey.toScreamingSnakeCase(),
        javaField = fieldName(jsonKey),
        getter = getterName(jsonKey),
        builder = builderName(jsonKey),
        typeExpr = resolved.expr,
        typeImports = resolved.imports,
        description = propNode["description"]?.asText(),
      )
    }

    // Types from model.internal produce an import; same-package types produce none.
    data class ExpPropInfo(val jsonKey: String, val typeExpr: String, val typeImport: String?)
    val expProps = if (isStable) experimentalPropPairs.map { (jsonKey, propNode) ->
      val resolved = resolvePropertyType(propNode, pkg)
      ExpPropInfo(jsonKey, resolved.expr, resolved.imports.firstOrNull())
    } else emptyList()

    // Non-list stable properties are candidates for graduated /development key detection.
    val stableObjectProps = if (isStable) fields.filter { !it.typeExpr.startsWith("List<") } else emptyList()

    val companionName = if (isExtensible && !isStable) "${defKey}PropertyModel" else null
    val additionalPropsType = when {
      isExtensible && !isStable -> "Map<String, $companionName>"
      hasOpenAdditionalProperties && !isStable -> "Map<String, Object>"
      else -> null
    }
    val allowsAdditionalProperties = isStable && (isExtensible || hasOpenAdditionalProperties)
    // Only generate extension property infrastructure when it serves a purpose: current
    // experimental properties to store, stable non-list properties as graduation candidates,
    // or an open schema acting as a ComponentProvider extension point.
    val needsExtensionProperties = isStable &&
      (expProps.isNotEmpty() || stableObjectProps.isNotEmpty() || allowsAdditionalProperties)

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
    if (isStable) {
      for (f in fields) {
        imports.add("static $pkg.$className.${f.constName}")
      }
    }
    if (needsExtensionProperties) {
      imports.addAll(listOf(
        "java.util.HashMap",
        "java.util.LinkedHashMap",
        "java.util.Map",
        "com.fasterxml.jackson.annotation.JsonAnyGetter",
        "com.fasterxml.jackson.annotation.JsonAnySetter",
        "$internalPackage.ExtensionPropertyUtil",
      ))
      if (expProps.isNotEmpty()) {
        val accessorFqcn = "$internalPackage.${classNameForKey(defKey)}Accessor"
        imports.add(accessorFqcn)
        imports.add("static $accessorFqcn.EXPERIMENTAL_PROPERTIES")
      }
      if (expProps.isEmpty() || stableObjectProps.isEmpty()) {
        imports.add("java.util.Collections")
      }
    }

    appendLicense()
    append("\npackage $pkg;\n\n")
    for (imp in imports) append("import $imp;\n")
    append("\n@JsonInclude(JsonInclude.Include.NON_NULL)\n")

    // @JsonPropertyOrder covers stable properties only — experimental entries are served via
    // @JsonAnyGetter and always append last regardless. Static self-imports make the class's
    // own package-private constants visible at the class-level annotation site.
    val propKeyRefs = if (isStable) {
      stablePropertyPairs.map { (jsonKey, _) -> jsonKey.toScreamingSnakeCase() }
    } else {
      allProperties.map { "\"${it.key}\"" }
    }
    if (propKeyRefs.isEmpty()) {
      append("@JsonPropertyOrder({})\n")
    } else {
      append("@JsonPropertyOrder({\n")
      propKeyRefs.forEachIndexed { i, k ->
        append("  $k${if (i < propKeyRefs.size - 1) "," else ""}\n")
      }
      append("})\n")
    }

    append("@Generated(\"io.opentelemetry.gradle.DeclarativeConfigPojoGenerator\")\n")
    append("public class $className {\n\n")

    if (isStable) {
      for (f in fields) {
        append("  static final String ${f.constName} = \"${f.jsonKey}\";\n")
      }
      if (fields.isNotEmpty()) append("\n")
    }
    if (needsExtensionProperties) {
      // STABLE_PROPERTIES maps non-list stable property names to their types for graduated
      // /development key detection. Omitted when empty.
      if (stableObjectProps.isNotEmpty()) {
        append("  private static final Map<String, Class<?>> STABLE_PROPERTIES;\n")
        append("  static {\n    STABLE_PROPERTIES = new HashMap<>();\n")
        for (f in stableObjectProps) {
          append("    STABLE_PROPERTIES.put(${f.constName}, ${f.typeExpr}.class);\n")
        }
        append("  }\n")
      }
      append("  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = $allowsAdditionalProperties;\n")
      append("\n")
    }
    for (f in fields) append("  @Nullable private ${f.typeExpr} ${f.javaField};\n")
    // Non-stable extensible types keep their own typed additionalProperties map (unchanged).
    if (additionalPropsType != null) {
      val init = if (companionName != null) "new LinkedHashMap<String, $companionName>()"
      else "new LinkedHashMap<String, Object>()"
      append("  private $additionalPropsType additionalProperties = $init;\n")
    }
    if (needsExtensionProperties) {
      append("  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();\n")
    }
    if (fields.isNotEmpty() || additionalPropsType != null || needsExtensionProperties) append("\n")

    for (f in fields) {
      if (f.description != null) {
        append("  /**\n")
        for (line in formatJavadoc(f.description)) append("   * $line\n")
        append("   */\n")
      }
      val jsonPropRef = if (isStable) f.constName else "\"${f.jsonKey}\""
      append("  @JsonProperty($jsonPropRef)\n")
      append("  @Nullable\n")
      // Non-list stable getters on classes with extension properties fall back to reading a
      // graduated /development value from extensionProperties. The stable field name is passed
      // through; ExtensionPropertyUtil.getGraduated internally appends the /development suffix
      // to match handleAnySetter's storage key.
      if (needsExtensionProperties && !f.typeExpr.startsWith("List<")) {
        append("  public ${f.typeExpr} ${f.getter}() {\n")
        append("    if (${f.javaField} == null) {\n")
        append("      return ExtensionPropertyUtil.getGraduated(${f.constName}, extensionProperties, ${f.typeExpr}.class);\n")
        append("    }\n")
        append("    return ${f.javaField};\n")
        append("  }\n\n")
      } else {
        append("  public ${f.typeExpr} ${f.getter}() {\n    return ${f.javaField};\n  }\n\n")
      }
      append("  @JsonProperty($jsonPropRef)\n")
      append("  public $className ${f.builder}(${f.typeExpr} ${f.javaField}) {\n")
      append("    this.${f.javaField} = ${f.javaField};\n    return this;\n  }\n\n")
    }

    // Non-stable extensible types: unchanged additionalProperties getter/setter.
    if (additionalPropsType != null) {
      val apValueType = companionName ?: "Object"
      append("  @JsonAnyGetter\n")
      append("  public $additionalPropsType getAdditionalProperties() {\n    return this.additionalProperties;\n  }\n\n")
      append("  @JsonAnySetter\n")
      append("  public $className withAdditionalProperty(String name, $apValueType value) {\n")
      append("    this.additionalProperties.put(name, value);\n    return this;\n  }\n\n")
    }

    if (needsExtensionProperties) {
      append("  @JsonAnyGetter\n")
      append("  public Map<String, Object> getExtensionProperties() {\n")
      if (stableObjectProps.isNotEmpty()) {
        append("    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);\n")
      } else {
        append("    return extensionProperties;\n")
      }
      append("  }\n\n")
      append("  @JsonAnySetter\n")
      append("  public $className withExtensionProperty(String name, @Nullable Object value) {\n")
      append("    ExtensionPropertyUtil.handleAnySetter(\n")
      val experimentalPropsArg = if (expProps.isNotEmpty()) "EXPERIMENTAL_PROPERTIES" else "Collections.emptyMap()"
      val stablePropsArg = if (stableObjectProps.isNotEmpty()) "STABLE_PROPERTIES" else "Collections.emptyMap()"
      append("        name, value, extensionProperties, $experimentalPropsArg, $stablePropsArg,\n")
      append("        ALLOWS_ADDITIONAL_PROPERTIES);\n")
      append("    return this;\n  }\n\n")
    }

    val allFields = fields.map { it.javaField } +
      (if (additionalPropsType != null) listOf("additionalProperties") else emptyList()) +
      (if (needsExtensionProperties) listOf("extensionProperties") else emptyList())

    appendToString(className, allFields)
    append("\n")
    appendHashCode(allFields)
    append("\n")
    appendEquals(className, allFields)
    append("}\n")
  }

  private fun buildExperimentalAccessorSource(
    defKey: String,
    stablePkg: String,
    experimentalPropPairs: List<Map.Entry<String, JsonNode>>,
  ) = buildString {
    val stableClassName = classNameForKey(defKey)
    val accessorClassName = "${stableClassName}Accessor"
    val stableImport = "$stablePkg.$stableClassName"

    data class PropInfo(
      val jsonKey: String,
      val constName: String, // SCREAMING_SNAKE_CASE key constant, /development suffix stripped
      val getter: String,
      val builder: String,
      val typeExpr: String,
    )
    val props = experimentalPropPairs.map { (jsonKey, propNode) ->
      val baseKey = jsonKey.removeSuffix("/development")
      val typeExpr = resolvePropertyType(propNode, internalPackage).expr
      // The accessor emits ${typeExpr}.class in EXPERIMENTAL_PROPERTIES. Parameterized types like
      // List<X> would produce non-compiling Java. Fail fast so a schema change adding a
      // list-typed /development property is caught at generation time rather than emitting
      // broken source.
      if (typeExpr.startsWith("List<")) {
        error(
          "Experimental property '$jsonKey' on '$defKey' has list type '$typeExpr'; the " +
            "generator does not support list-typed experimental properties (see " +
            "EXPERIMENTAL_PROPERTIES emission in buildExperimentalAccessorSource)."
        )
      }
      PropInfo(
        jsonKey = jsonKey,
        constName = baseKey.toScreamingSnakeCase(),
        getter = getterName(baseKey),
        builder = builderName(baseKey),
        typeExpr = typeExpr,
      )
    }

    appendLicense()
    append("\npackage $internalPackage;\n\n")
    append("import $stableImport;\n")
    append("import java.util.HashMap;\n")
    append("import java.util.Map;\n")
    append("import static java.util.Objects.requireNonNull;\n")
    append("import javax.annotation.Nullable;\n")
    append("\n")
    append("/**\n")
    append(" * Provides typed access to experimental properties on {@link $stableClassName}.\n")
    append(" *\n")
    append(" * <p>This class is internal and experimental. Its APIs are unstable and can change at any\n")
    append(" * time. Its APIs (or a version of them) may be promoted to the public stable API in the\n")
    append(" * future, but no guarantees are made.\n")
    append(" */\n")
    append("public final class $accessorClassName {\n\n")
    append("  private $accessorClassName() {}\n\n")

    for (p in props) {
      append("  static final String ${p.constName} = \"${p.jsonKey}\";\n")
    }
    append("\n")

    append("  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;\n")
    append("  static {\n    EXPERIMENTAL_PROPERTIES = new HashMap<>();\n")
    for (p in props) {
      append("    EXPERIMENTAL_PROPERTIES.put(${p.constName}, ${p.typeExpr}.class);\n")
    }
    append("  }\n\n")

    for (p in props) {
      append("  @Nullable\n")
      append("  public static ${p.typeExpr} ${p.getter}($stableClassName model) {\n")
      append("    return ExtensionPropertyUtil.get(\n")
      append("        ${p.constName}, model.getExtensionProperties(), ${p.typeExpr}.class);\n")
      append("  }\n\n")

      append("  public static $stableClassName ${p.builder}(\n")
      append("      $stableClassName model, ${p.typeExpr} value) {\n")
      append("    requireNonNull(value, \"value\");\n")
      append("    model.withExtensionProperty(${p.constName}, value);\n")
      append("    return model;\n")
      append("  }\n\n")
    }

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
   * paragraph break so google-java-format doesn't collapse them into one line. Empty lines from
   * double-newline paragraph separators in the schema are skipped — the following non-empty line
   * opens its own paragraph.
   */
  private fun formatJavadoc(description: String): List<String> {
    val lines = description.trimEnd('\n').split('\n')
    if (lines.size == 1) return lines
    return buildList {
      lines.forEachIndexed { i, line ->
        when {
          i == 0 -> add(line)
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
