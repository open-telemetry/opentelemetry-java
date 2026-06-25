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
