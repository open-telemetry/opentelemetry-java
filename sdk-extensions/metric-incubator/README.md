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
           .setInstrumentName("my-instrument")
           .setInstrumentType(InstrumentType.COUNTER)
           .setMeterSelector(
               MeterSelector.builder()
                   .setName("my-meter")
                   .setVersion("1.0.0")
                   .setSchemaUrl("http://example.com")
                   .build())
           .build(),
       View.builder()
           .setName("new-instrument")
           .setDescription("new-description")
           .setAggregation(Aggregation.histogram())
           .filterAttributes(key -> new HashSet<>(Arrays.asList("foo", "bar")).contains(key))
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
ViewConfig.registerViews(builder, new File("/Users/user123/view.yaml"));
```

Notes on usage:

- Many view configurations can live in one file. The YAML is parsed as an array of view
  configurations.
- At least one selection field is required, but including all is not necessary. Any omitted fields
  will result in the default from `InstrumentSelector` being used.
- At least one view field is required, but including all is not required. Any omitted fields will
  result in the default from `View` being used.
- Advanced selection criteria, like regular expressions, is not yet supported.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-metric-incubator

[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-metric-incubator
