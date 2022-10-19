package io.opentelemetry.gradle

import com.squareup.wire.schema.SchemaHandler

class ProtoFieldsWireHandlerFactory : SchemaHandler.Factory{
  override fun create(): SchemaHandler {
    return ProtoFieldsWireHandler()
  }
}