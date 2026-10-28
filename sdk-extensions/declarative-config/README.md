# Declarative Configuration

The [declarative configuration interface](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/README.md#declarative-configuration) allows for YAML based file configuration of `OpenTelemetrySdk`.

Usage:

```shell
File yamlConfigFile = new File("/path/to/config.yaml");
OpenTelemetrySdk openTelemetrySdk;
try (FileInputStream yamlConfigFileInputStream = new FileInputStream("/path/to/config.yaml")) {
  openTelemetrySdk = DeclarativeConfiguration.parseAndCreate(yamlConfigFileInputStream).getSdk();
}
// ...proceed with application after successful initialization of OpenTelemetrySdk
```

Notes:
* Environment variable substitution is supported as [defined in the spec](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/data-model.md#environment-variable-substitution)
* Customization is limited to customizing the in-memory config model, or exporters. See `DeclarativeConfigurationCustomizer` SPI for details.
* YAML parsing and model mapping can be replaced with `DeclarativeConfigurationYamlParser` and `DeclarativeConfigurationMapper` implementations. Both interfaces are in the `opentelemetry-sdk-extension-declarative-config-spi` artifact and are included transitively. Providers are discovered with `ServiceLoader`; the provider with the highest `order()` is selected.
* The built-in SnakeYAML parser and Jackson 2 mapper work by default. The Jackson 2 mapper is supplied by a runtime adapter. If you exclude that adapter, register a mapper provider of your own; otherwise, operations that need mapping fail with a `DeclarativeConfigException`.
* Custom SDK extension components which reference the [ComponentProvider](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure-spi/src/main/java/io/opentelemetry/sdk/autoconfigure/spi/internal/ComponentProvider.java) SPI can be referenced in declarative configuration. Supported types include:
  * `Resource`
  * `SpanExporter`
  * `MetricExporter`
  * `LogRecordExporter`
  * `SpanProcessor`
  * `LogRecordProcessor`
  * `TextMapPropagator`
  * `Sampler`
* You can use declarative configuration with [autoconfigure](https://opentelemetry.io/docs/languages/java/configuration/#declarative-configuration) to specify a configuration file via environment variable, e.g. `OTEL_CONFIG_FILE=/path/to/config.yaml`.

## Use a custom mapper

To use a mapper such as Jackson 3, exclude the default Jackson 2 adapter from the declarative config
dependency and add Jackson 3 Databind. This Gradle example uses Jackson 3.2.3:

```kotlin
dependencies {
  implementation("io.opentelemetry:opentelemetry-sdk-extension-declarative-config") {
    exclude(
      group = "io.opentelemetry",
      module = "opentelemetry-sdk-extension-declarative-config-jackson",
    )
  }
  implementation("tools.jackson.core:jackson-databind:3.2.3")
}
```

Implement `DeclarativeConfigurationMapper` and override these methods:

* `order()` returns the provider priority. The provider with the highest value is selected.
* `convert(Object value, Class<T> targetType)` converts parsed configuration values to a model.
* `toMap(Object value)` converts a model or configuration value to a map with string keys. Return
  `null` when the input is `null`.

Configure your mapper to preserve the null and extension-property behavior expected by the models.

Register the class with `ServiceLoader` by adding this file to your application or mapper module:

```text
META-INF/services/io.opentelemetry.sdk.autoconfigure.declarativeconfig.spi.DeclarativeConfigurationMapper
```

Put your provider's fully qualified class name in that file:

```text
your.package.YourMapper
```

With the service file on the runtime classpath, declarative config discovers and uses your mapper.
