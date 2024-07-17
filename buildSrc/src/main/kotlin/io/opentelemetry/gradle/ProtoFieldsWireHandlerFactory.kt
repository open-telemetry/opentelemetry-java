package io.opentelemetry.gradle

import com.squareup.wire.schema.SchemaHandler

class ProtoFieldsWireHandlerFactory : SchemaHandler.Factory{

  override fun create(
    includes: List<String>,
    excludes: List<String>,
    exclusive: Boolean,
    outDirectory: String,
    options: Map<String, String>
  ): SchemaHandler {
    return ProtoFieldsWireHandler()
  }

}
