# OpenTelemetry Kotlin Extension

Kotlin [Extensions](src/main/kotlin/io/opentelemetry/extension/kotlin/ContextExtensions.kt) to propagate
OpenTelemetry context into coroutines.

For example, you could do the following with coroutines

```kotlin
launch(Context.current().asContextElement()) {
// trace ids propagated here
}
```
