package io.opentelemetry.gradle

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.wire.WireCompiler
import com.squareup.wire.WireLogger
import com.squareup.wire.schema.CustomHandlerBeta
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.Extend
import com.squareup.wire.schema.Field
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.ProfileLoader
import com.squareup.wire.schema.ProtoFile
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.Service
import com.squareup.wire.schema.Target
import com.squareup.wire.schema.Type
import okio.IOException
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

// Wire proto compiler provides this convenient interface for customizing the output of proto
// compilation. We use it to generate classes that only contain field numbers and enum values, which
// we use in our custom Marshaler from SDK types to OTLP types, skipping the otherwise hefty
// generated protoc code.
//
// Inspired by https://github.com/square/wire/blob/5fac94f86879fdd7e412cddbeb51e09a708b2b64/wire-library/wire-compiler/src/main/java/com/squareup/wire/schema/Target.kt#L152
class ProtoFieldsWireHandler : CustomHandlerBeta {
  override fun newHandler(
    schema: Schema,
    fs: FileSystem,
    outDirectory: String,
    logger: WireLogger,
    profileLoader: ProfileLoader
  ): Target.SchemaHandler {
    val modulePath = fs.getPath(outDirectory)
    Files.createDirectories(modulePath)
    val javaGenerator = JavaGenerator.get(schema)

    return object : Target.SchemaHandler {
      override fun handle(extend: Extend, field: Field) = null
      override fun handle(service: Service): List<Path> = emptyList()
      override fun handle(type: Type): Path? {
        val typeSpec = javaGenerator.generateType(type)
        val javaTypeName = javaGenerator.generatedTypeName(type)

        if (typeSpec == null) {
          return null
        }

        val javaFile = JavaFile.builder(javaTypeName.packageName(), typeSpec)
          .addFileComment("\$L", WireCompiler.CODE_GENERATED_BY_WIRE)
          .addFileComment("\nSource: \$L in \$L", type.type, type.location.withPathOnly())
          .build()
        val generatedFilePath = modulePath.resolve(javaFile.packageName)
          .resolve("${javaFile.typeSpec.name}.java")

        logger.artifact(modulePath, javaFile)
        try {
          javaFile.writeTo(modulePath)
        } catch (e: IOException) {
          throw IOException("Error emitting ${javaFile.packageName}.${javaFile.typeSpec.name} " +
            "to $outDirectory", e)
        }
        return generatedFilePath
      }
    }
  }

  private class JavaGenerator(private val typeToJavaName: Map<ProtoType, TypeName>) {

    companion object {
      private val STRING_TYPE_NAME = ClassName.get("java.lang", "String")

      fun get(schema: Schema): JavaGenerator {
        val nameToJavaName = linkedMapOf<ProtoType, TypeName>()
        for (protoFile in schema.protoFiles) {
           val javaPackage = javaPackage(protoFile)
          putAll(nameToJavaName, javaPackage, null, protoFile.types)
        }

        return JavaGenerator(nameToJavaName)
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

    fun generateType(type: Type): TypeSpec? {
      if (type is MessageType) {
        return generateMessage(type)
      }
      if (type is EnumType) {
        return generateEnum(type)
      }
      return null
    }

    fun generatedTypeName(type: Type): ClassName {
      return typeToJavaName[type.type] as ClassName
    }

    private fun generateMessage(type: MessageType): TypeSpec {
      val javaType = typeToJavaName[type.type] as ClassName

      val builder = TypeSpec.classBuilder(javaType.simpleName())
        .addModifiers(PUBLIC, FINAL)

      for (field in type.fieldsAndOneOfFields) {
        builder.addField(
          FieldSpec.builder(TypeName.INT, "${field.name.toUpperCase()}_FIELD_NUMBER", PUBLIC, STATIC, FINAL)
            .initializer("\$L", field.tag)
            .build())
        builder.addField(
          FieldSpec.builder(STRING_TYPE_NAME, "${field.name.toUpperCase()}_JSON_NAME", PUBLIC, STATIC, FINAL)
            .initializer("\"\$L\"", field.jsonName)
            .build())
      }

      for (nestedType in type.nestedTypes) {
        builder.addType(generateType(nestedType))
      }

      return builder.build()
    }

    private fun generateEnum(type: EnumType): TypeSpec {
      val javaType = typeToJavaName[type.type] as ClassName

      val builder = TypeSpec.classBuilder(javaType.simpleName())
        .addModifiers(PUBLIC, FINAL)

      for (constant in type.constants) {
        builder.addField(
          FieldSpec.builder(TypeName.INT, "${constant.name}_VALUE", PUBLIC, STATIC, FINAL)
            .initializer("\$L", constant.tag)
            .build())
      }

      return builder.build()
    }
  }
}
