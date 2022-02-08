# OpenTelemetry View Config SDK Extension

[![Javadocs][javadoc-image]][javadoc-url]

This artifact contains experimental code for enabling file based YAML configuration of Metric SDK Views.

For example, a YAML file at `/Users/user123/view.yaml` could be used to configure views by calling:

```
SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
ViewConfig.registerViews(builder, new File("/Users/user123/view.yaml"));
```

Suppose `/Users/user123/view.yaml` had content as follows:

```yaml
- selector:
    instrumentName: my-instrument
    instrumentType: COUNTER
    meterName: my-meter
    meterVersion: 1.0.0
    meterSchemaUrl: http://example.com
  view:
    name: new-instrument-name
    description: new-description
    aggregation: histogram
```

That would produce a view configuration equivalent to:

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
           .build());
```

Notes on usage:

- Many view configurations can live in one file. The YAML is parsed as an array of view
  configurations.
- At least one selection field is required, but including all is not necessary. Any omitted fields
  will result in the default from `InstrumentSelector` being used.
- At least one view field is required, but including all is not required. Any omitted fields will
  result in the default from `View` being used.
- Advanced selection criteria, like regular expressions, is not yet supported.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-extension-view-config.svg

[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-extension-view-config
