# OpenTelemetry Incubator

This artifact contains experimental code related to the trace and metric SDKs.

## Declarative Configuration

The [declarative configuration interface](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/README.md#declarative-configuration) allows for YAML based file configuration of `OpenTelemetrySdk`.

Usage:

```shell
File yamlConfigFile = new File("/path/to/config.yaml");
OpenTelemetrySdk openTelemetrySdk;
try (FileInputStream yamlConfigFileInputStream = new FileInputStream("/path/to/config.yaml")) {
  openTelemetrySdk = FileConfiguration.parseAndCreate(yamlConfigFileInputStream);
}
// ...proceed with application after successful initialization of OpenTelemetrySdk
```

Notes:
* Environment variable substitution is supported as [defined in the spec](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/data-model.md#environment-variable-substitution)
* Currently, there is no support for the customization (i.e. `AutoConfigurationCustomizerProvider`) SPIs defined in [opentelemetry-sdk-extension-autoconfigure-spi](../autoconfigure-spi).
* Custom SDK extension components which reference the [ComponentProvider](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure-spi/src/main/java/io/opentelemetry/sdk/autoconfigure/spi/internal/ComponentProvider.java) SPI can be referenced in declarative configuration. Supported types include:
  * `Resource`
  * `SpanExporter`
  * `MetricExporter`
  * `LogRecordExporter`
  * `SpanProcessor`
  * `LogRecordProcessor`
  * `TextMapPropagator`
  * `Sampler`
* You can use declarative configuration with [autoconfigure](https://opentelemetry.io/docs/languages/java/configuration/#declarative-configuration) to specify a configuration file via environment variable, e.g. `OTEL_EXPERIMENTAL_CONFIG_FILE=/path/to/config.yaml`.

## View File Configuration

Adds support for file based YAML configuration of Metric SDK Views.

For example, suppose `/Users/user123/view.yaml` has the following content:

```yaml
- selector:
    instrument_name: my-instrument
    instrument_type: COUNTER
    instrument_unit: ms
    meter_name: my-meter
    meter_version: 1.0.0
    meter_schema_url: http://example.com
  view:
    name: new-instrument-name
    description: new-description
    aggregation: explicit_bucket_histogram
    aggregation_args:
      bucket_boundaries: [1.0, 2.0, 5.0]
    attribute_keys:
      - foo
      - bar
```

The equivalent view configuration would be:

```
SdkMeterProvider.builder()
   .registerView(
       InstrumentSelector.builder()
           .setName("my-instrument")
           .setType(InstrumentType.COUNTER)
           .setUnit("ms")
           .setMeterName("my-meter")
           .setMeterVersion("1.0.0")
           .setMeterSchemaUrl("http://example.com")
           .build(),
       View.builder()
           .setName("new-instrument")
           .setDescription("new-description")
           .setAggregation(Aggregation.explicitBucketHistogram(Arrays.asList(1.0, 2.0, 5.0))
           .setAttributesFilter(key -> new HashSet<>(Arrays.asList("foo", "bar")).contains(key))
           .build());
```

If using [autoconfigure](../autoconfigure) with this artifact on your classpath, it will automatically load a list of view config files specified via environment variable or system property:

| System property                       | Environment variable                  | Purpose                                              |
|---------------------------------------|---------------------------------------|------------------------------------------------------|
| otel.experimental.metrics.view-config | OTEL_EXPERIMENTAL_METRICS_VIEW_CONFIG | List of files containing view configuration YAML [1] |

**[1]** In addition to absolute paths, resources on the classpath packaged with a jar can be loaded.
For example, `otel.experimental.metrics.view-config=classpath:/my-view.yaml` loads the
resource `/my-view.yaml`.

If not using autoconfigure, a file can be used to configure views as follows:

```
SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
try (FileInputStream fileInputStream = new FileInputStream("/Users/user123/view.yaml")) {
  ViewConfig.registerViews(builder, fileInputStream);
}
```

The following table describes the set of recognized aggregations:

| Aggregation                      | Arguments                                                                                                                      |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| [`default`]                      | -                                                                                                                              |
| [`sum`]                          | -                                                                                                                              |
| [`last_value`]                   | -                                                                                                                              |
| [`drop`]                         | -                                                                                                                              |
| [`explicit_bucket_histogram`]    | `bucket_boundaries` (optional): List of inclusive upper boundaries for the histogram buckets, in order from lowest to highest. |
| [`exponential_bucket_histogram`] | `max_buckets` (optional): The maximum number of buckets to use for positive or negative recordings.                            |

Additional notes on usage:

- Many view configurations can live in one file. The YAML is parsed as an array of view
  configurations.
- At least one selection field is required, but including all is not necessary. Any omitted fields
  will result in the default from `InstrumentSelector` being used.
- At least one view field is required, but including all is not required. Any omitted fields will
  result in the default from `View` being used.
- Instrument name selection supports the following wildcard characters: `*` matches 0 or more instances of any character; `?` matches exactly one instance of any character. No other advanced selection criteria is supported.

[`default`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#default-aggregation
[`sum`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#sum-aggregation
[`last_value`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#last-value-aggregation
[`drop`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#drop-aggregation
[`explicit_bucket_histogram`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#explicit-bucket-histogram-aggregation
[`exponential_bucket_histogram`]: https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#base2-exponential-bucket-histogram-aggregation
