# OpenTelemetry Metric Incubator

[![Javadocs][javadoc-image]][javadoc-url]

This artifact contains experimental code related to metrics.

## View File Configuration

Adds support for file based YAML configuration of Metric SDK Views.

For example, suppose `/Users/user123/view.yaml` has the following content:

```yaml
- selector:
    instrument_name: my-instrument
    instrument_type: COUNTER
    meter_name: my-meter
    meter_version: 1.0.0
    meter_schema_url: http://example.com
  view:
    name: new-instrument-name
    description: new-description
    aggregation: histogram
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
           .setMeterName("my-meter")
           .setMeterVersion("1.0.0")
           .setMeterSchemaUrl("http://example.com")
           .build(),
       View.builder()
           .setName("new-instrument")
           .setDescription("new-description")
           .setAggregation(Aggregation.histogram())
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

Notes on usage:

- Many view configurations can live in one file. The YAML is parsed as an array of view
  configurations.
- At least one selection field is required, but including all is not necessary. Any omitted fields
  will result in the default from `InstrumentSelector` being used.
- At least one view field is required, but including all is not required. Any omitted fields will
  result in the default from `View` being used.
- Instrument name selection supports the following wildcard characters: `*` matches 0 or more instances of any character; `?` matches exactly one instance of any character. No other advanced selection criteria is supported.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-metric-incubator.svg

[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-metric-incubator
