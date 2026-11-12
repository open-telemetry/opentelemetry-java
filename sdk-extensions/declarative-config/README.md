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
* The parser and mapper are internal, unstable SPIs in
  `opentelemetry-sdk-extension-autoconfigure-spi` (`io.opentelemetry.sdk.autoconfigure.spi.internal`).
  `ServiceLoader` loads providers; when several are present, the first is used with a warning unless
  selected through the corresponding system property or environment variable.
* The SnakeYAML parser and Jackson 2 mapper adapters are included by default. Exclude an adapter to
  use a custom provider. If no provider is available, the operation fails with a
  `DeclarativeConfigException`.
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

To use a mapper such as Jackson 3, exclude the default
`opentelemetry-sdk-extension-declarative-config-jackson2` adapter from the declarative config
dependency and add Jackson 3 Databind. This Gradle example uses Jackson 3.2.3:

```kotlin
dependencies {
  implementation("io.opentelemetry:opentelemetry-sdk-extension-declarative-config") {
    exclude(
      group = "io.opentelemetry",
      module = "opentelemetry-sdk-extension-declarative-config-jackson2",
    )
  }
  implementation("tools.jackson.core:jackson-databind:3.2.3")
}
```

Implement `DeclarativeConfigurationMapper` and override these methods:

* `convert(Object value, Class<T> targetType)` converts parsed configuration values to a model.
* `toMap(Object value)` converts a model or configuration value to a map with string keys. Return
  `null` when the input is `null`.

Mapper methods and `DeclarativeConfigurationYamlParser.parse` report conversion and parsing
failures with `DeclarativeConfigException`. The declarative configuration implementation wraps
other runtime exceptions thrown by providers in this exception.

Configure your mapper to preserve the null and extension-property behavior expected by the models.

Register the class with `ServiceLoader` by adding this file to your application or mapper module:

```text
META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationMapper
```

Put your provider's fully qualified class name in that file:

```text
your.package.YourMapper
```

With the service file on the runtime classpath, declarative config discovers your mapper.

## Select a mapper

If exactly one mapper provider is on the classpath, it is used. If multiple providers are present,
select one by setting the following system property or environment variable to the provider's fully
qualified class name:

| Configuration source | Name |
| --- | --- |
| System property | `io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationMapper` |
| Environment variable | `IO_OPENTELEMETRY_SDK_AUTOCONFIGURE_SPI_INTERNAL_DECLARATIVECONFIGURATIONMAPPER` |

When multiple providers are present and neither setting is set, the first discovered provider is
used and a warning is logged. If the setting names a provider that is not present, declarative
configuration fails with an `IllegalStateException`.

## Use a custom YAML parser

To replace the default SnakeYAML parser, exclude the
`opentelemetry-sdk-extension-declarative-config-snakeyaml` adapter from the declarative config
dependency:

```kotlin
dependencies {
  implementation("io.opentelemetry:opentelemetry-sdk-extension-declarative-config") {
    exclude(
      group = "io.opentelemetry",
      module = "opentelemetry-sdk-extension-declarative-config-snakeyaml",
    )
  }
}
```

Implement `DeclarativeConfigurationYamlParser` and register it with `ServiceLoader` by adding this
file to your application or parser module:

```text
META-INF/services/io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationYamlParser
```

Put your parser's fully qualified class name in the service file. The parser receives the YAML
stream and the environment variables and system properties to use for substitution.

## Select a YAML parser

If exactly one YAML parser provider is on the classpath, it is used. If multiple providers are
present, select one by setting the following system property or environment variable to the
provider's fully qualified class name:

| Configuration source | Name |
| --- | --- |
| System property | `io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationYamlParser` |
| Environment variable | `IO_OPENTELEMETRY_SDK_AUTOCONFIGURE_SPI_INTERNAL_DECLARATIVECONFIGURATIONYAMLPARSER` |

When multiple providers are present and neither setting is set, the first discovered provider is
used and a warning is logged. If the setting names a provider that is not present, declarative
configuration fails with an `IllegalStateException`.
