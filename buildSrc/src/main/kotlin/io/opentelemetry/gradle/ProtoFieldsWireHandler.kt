package io.opentelemetry.gradle

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.wire.WireCompiler
import com.squareup.wire.WireLogger
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Extend
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.ProfileLoader
import com.squareup.wire.schema.ProtoFile
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.SchemaHandler
import com.squareup.wire.schema.Service
import com.squareup.wire.schema.Target
import com.squareup.wire.schema.Type
import okio.IOException
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

// Wire proto compiler provides this convenient interface for customizing the output of proto
// compilation. We use it to generate classes that only contain field numbers and enum values, which
// we use in our custom Marshaler from SDK types to OTLP types, skipping the otherwise hefty
// generated protoc code.
//
// Inspired by https://github.com/square/wire/blob/5fac94f86879fdd7e412cddbeb51e09a708b2b64/wire-library/wire-compiler/src/main/java/com/squareup/wire/schema/Target.kt#L152
class ProtoFieldsWireHandler : SchemaHandler() {

  private var schema: Schema?= null
  private var javaGenerator: JavaGenerator?= null

  override fun handle(schema: Schema, context: Context) {
    if (this.schema != null && this.schema != schema) {
      throw IllegalStateException("Cannot use same handler with multiple schemas")
    }
    if (this.schema == null) {
      this.schema = schema
      this.javaGenerator = JavaGenerator.get(schema)
    }
    super.handle(schema, context)
  }

  override fun handle(service: Service, context: Context): List<Path> = emptyList()

  override fun handle(extend: Extend, field: Field, context: Context): Path? = null

  override fun handle(type: Type, context: Context): Path? {
    val fs = context.fileSystem
    val outDirectory = context.outDirectory

    val typeSpec = javaGenerator!!.generateType(type, false)
    val javaTypeName = javaGenerator!!.generatedTypeName(type)

    if (typeSpec == null) {
      return null
    }

    val javaFile = JavaFile.builder(javaTypeName.packageName(), typeSpec)
      .addFileComment("\$L", WireCompiler.CODE_GENERATED_BY_WIRE)
      .addFileComment("\nSource: \$L in \$L", type.type, type.location.withPathOnly())
      .build()
    val generatedFilePath = outDirectory / javaFile.packageName / "${javaFile.typeSpec.name}.java"

    val filePath = outDirectory /
      javaFile.packageName.replace(".", "/") /
      "${javaTypeName.simpleName()}.java"

    try {
      fs.createDirectories(filePath.parent!!)
      fs.write(filePath) {
        writeUtf8(javaFile.toString())
      }
    } catch (e: IOException) {
      throw IOException("Error emitting ${javaFile.packageName}.${javaFile.typeSpec.name} " +
        "to $outDirectory", e)
    }
    return generatedFilePath
  }

  private class JavaGenerator(private val schema: Schema, private val typeToJavaName: Map<ProtoType, TypeName>) {

    companion object {
      private val PROTO_FIELD_INFO = ClassName.get("io.opentelemetry.exporter.internal.marshal", "ProtoFieldInfo")
      private val PROTO_ENUM_INFO = ClassName.get("io.opentelemetry.exporter.internal.marshal", "ProtoEnumInfo")
      private val WIRETYPE_VARINT = 0
      private val WIRETYPE_FIXED64 = 1
      private val WIRETYPE_LENGTH_DELIMITED = 2
      private val WIRETYPE_FIXED32 = 5

      fun get(schema: Schema): JavaGenerator {
        val nameToJavaName = linkedMapOf<ProtoType, TypeName>()
        for (protoFile in schema.protoFiles) {
          if (protoFile.location.path == "wire/extensions.proto") {
            continue
          }
          val javaPackage = javaPackage(protoFile)
          putAll(nameToJavaName, javaPackage, null, protoFile.types)
        }

        return JavaGenerator(schema, nameToJavaName)
      }

      private fun putAll(
        wireToJava: MutableMap<ProtoType, TypeName>,
        javaPackage: String,
        enclosingClassName: ClassName?,
        types: List<Type>) {
        for (type in types) {
          val className = enclosingClassName?.let {
            it.nestedClass(type.type.simpleName)
          } ?: ClassName.get(javaPackage, type.type.simpleName)
          wireToJava[type.type] = className
          putAll(wireToJava, javaPackage, className, type.nestedTypes)
        }
      }

      private fun javaPackage(protoFile: ProtoFile): String {
        val javaPackage = protoFile.javaPackage()
        if (javaPackage == null) {
          throw IOException("Attempting to generate Java for proto without java_package")
        }
        // Just append .internal to the defined package to hold our trimmed ones.
        return "${javaPackage}.internal"
      }
    }

    fun generateType(type: Type, nested: Boolean): TypeSpec? {
      if (type is MessageType) {
        return generateMessage(type, nested)
      }
      if (type is EnumType) {
        return generateEnum(type, nested)
      }
      return null
    }

    fun generatedTypeName(type: Type): ClassName {
      return typeToJavaName[type.type] as ClassName
    }

    private fun generateMessage(type: MessageType, nested: Boolean): TypeSpec {
      val javaType = typeToJavaName[type.type] as ClassName

      val builder = TypeSpec.classBuilder(javaType.simpleName())
        .addModifiers(PUBLIC, FINAL)
      if (nested) {
        builder.addModifiers(STATIC)
      }

      for (field in type.fieldsAndOneOfFields) {
        builder.addField(
          FieldSpec.builder(PROTO_FIELD_INFO, field.name.uppercase(), PUBLIC, STATIC, FINAL)
            .initializer("\$T.create(\$L, \$L, \"\$L\")",
              PROTO_FIELD_INFO,
              field.tag,
              makeTag(field.tag, field.type as ProtoType, field.isRepeated),
              field.jsonName)
            .build())
      }

      for (nestedType in type.nestedTypes) {
        builder.addType(generateType(nestedType, true))
      }

      return builder.build()
    }

    private fun generateEnum(type: EnumType, nested: Boolean): TypeSpec {
      val javaType = typeToJavaName[type.type] as ClassName

      val builder = TypeSpec.classBuilder(javaType.simpleName())
        .addModifiers(PUBLIC, FINAL)
      if (nested) {
        builder.addModifiers(STATIC)
      }

      for (constant in type.constants) {
        builder.addField(
          FieldSpec.builder(PROTO_ENUM_INFO, constant.name, PUBLIC, STATIC, FINAL)
            .initializer("\$T.create(\$L, \"\$L\")", PROTO_ENUM_INFO, constant.tag, constant.name)
            .build())
      }

      return builder.build()
    }

    private fun fieldEncoding(type: ProtoType, isRepeated: Boolean): Int {
      if (isRepeated) {
        // Repeated fields are always length delimited in proto3
        return WIRETYPE_LENGTH_DELIMITED
      }

      if (schema.getType(type) is EnumType) {
        return WIRETYPE_VARINT
      }

      if (!type.isScalar) {
        // Non-scalar and not enum is a message
        return WIRETYPE_LENGTH_DELIMITED
      }

      return when(type) {
        ProtoType.FIXED32,
        ProtoType.SFIXED32,
        ProtoType.FLOAT-> WIRETYPE_FIXED32
        ProtoType.FIXED64,
        ProtoType.SFIXED64,
        ProtoType.DOUBLE -> WIRETYPE_FIXED64
        ProtoType.BYTES,
        ProtoType.STRING -> WIRETYPE_LENGTH_DELIMITED
        else -> WIRETYPE_VARINT
      }
    }

    private fun makeTag(fieldNumber: Int, type: ProtoType, isRepeated: Boolean): Int {
      return (fieldNumber shl 3) or fieldEncoding(type, isRepeated)
    }
  }
}
