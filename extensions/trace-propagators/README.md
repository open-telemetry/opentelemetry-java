OpenTelemetry Contrib Trace Propagators
======================================================

[![Javadocs][javadoc-image]][javadoc-url]

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-contrib-trace-propagators.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-contrib-trace-propagators

This project contains [trace propagators](https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/context/api-propagators.md) that support
3rd-party propagation formats.  These propagators are here for convenience and are not officially
supported by the OpenTelemetry Java maintainers.

Issues/support for these propagators is only provided as a minimal "best effort", and critical
bugs should be filed with the respective vendors themselves.

* AWS X-Ray (file an issue here and mention @anuraaga)
* OpenTracing (file an issue here and mention @carlosalberto)
* B3 (file an issue on [the OpenZipkin repo](https://github.com/openzipkin/b3-propagation) that points here)
* Jaeger (file an issue on [the Jaeger repo](https://github.com/jaegertracing/jaeger) that points here)

Extension providers that do not receive adequate support/maintenance by their respective vendors 
will become candidates for future removal.