package io.opentelemetry.gradle

import com.squareup.wire.schema.SchemaHandler

class ProtoFieldsWireHandlerFactory : SchemaHandler.Factory{
  @Deprecated("deprecated in parent")
  override fun create(): SchemaHandler {
    return ProtoFieldsWireHandler()
  }

  override fun create(
    includes: List<String>,
    excludes: List<String>,
    exclusive: Boolean,
    outDirectory: String,
    options: Map<String, String>
  ): SchemaHandler {
    @Suppress("DEPRECATION")
    return create()
  }

}
