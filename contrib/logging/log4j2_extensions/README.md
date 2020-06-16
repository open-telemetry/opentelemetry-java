OpenTelemetry Extensions for Log4j 2.x
======================================================

[![Javadocs][javadoc-image]][javadoc-url]

This module contains extensions that support adding trace correlation information to your
Log4j logs. Several modules are included.

# Context Data Provider

An implementation of the Log4J `ContextDataProvider` class is included. This class is loaded
automatically via the Java ServiceProvider interface, so it works as long as this library is in
your runtime classpath. This class will add three fields to the [thread context](https://logging.apache.org/log4j/2.x/manual/thread-context.html)
for each log entry that happens within an open span. These fields can be addressed from Log4j
layouts. For example, this `PatternLayout` configuration will add request correlation fields to 
your logs:

```xml
<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} traceid='%X{traceid}' spanid='%X{spanid}' traceflags='%X{traceflags}' - %msg%n"/>
```

Similarly request correlation flags can be added to the JsonLayout:

```xml
<JsonLayout complete="false" compact="true">
    <KeyValuePair key="traceid" value="$${ctx:traceid}"/>
    <KeyValuePair key="spanid" value="$${ctx:spanid}"/>
    <KeyValuePair key="traceflags" value="$${ctx:traceflags}"/>
</JsonLayout>
```

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-contrib-logging-log4j2-extensions.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-contrib-logging-log4j2-extensions
