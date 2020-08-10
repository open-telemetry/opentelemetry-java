OpenTelemetry Extensions JMX Metric Gatherer
============================================

[![Javadocs][javadoc-image]][javadoc-url]

* Java 7 compatible.

[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-contrib-runtime-metrics.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-contrib-runtime-metrics

This utility provides an easy framework for gathering and reporting metrics based on queried
MBeans from a JMX server.  It loads a custom Groovy script and establishes a helpful, bound `otel`
object with methods for obtaining MBeans and constructing synchronous OpenTelemetry instruments:

### Usage

```bash
$ java -cp <opentelemetry-java jars> io.opentelemetry.extensions.metrics.jmx.JmxMetrics -config ./config.json
```

##### `config.json` example

```json
{
  "serviceUrl": "service:jmx:rmi:///jndi/rmi://<my-jmx-host>:<my-jmx-port>/jmxrmi",
  "groovyScript": "/opt/script.groovy",
  "intervalSeconds": 5,
  "exporterType": "otlp",
  "exporterEndpoint": "my-opentelemetry-collector:55680",
  "username": "my-username",
  "password": "my-password"
}
```

##### `script.groovy` example

```groovy
import io.opentelemetry.common.Labels

def loadMatches = otel.queryJmx("org.apache.cassandra.metrics:type=Storage,name=Load")
def load = loadMatches.first()

def lvr = otel.longValueRecorder(
        "cassandra.storage.load",
        "Size, in bytes, of the on disk data size this node manages",
        "By", [myConstantLabelKey:"myConstantLabelValue"]
)

lvr.record(load.Count, Labels.of("myKey", "myVal"))
```

As configured in the example, this metric extension will configure an otlp gRPC metric exporter
at the `exporterEndpoint` and establish an MBean server connection using the provided `serviceUrl`.
After loading the Groovy script whose path is specified via `groovyScript`, it will then run the
script on the specified `intervalSeconds` and export the resulting metrics.

### JMX Query Helpers

- `otel.queryJmx(String objectNameStr)`
   - This method will query the connected JMX application for the given `objectName`, which can
   include wildcards.  The return value will be a `List<GroovyMBean>` of zero or more
   [`GroovyMBean` objects](http://docs.groovy-lang.org/latest/html/api/groovy/jmx/GroovyMBean.html),
   which are conveniently wrapped to make accessing attributes on the MBean simple.
   See http://groovy-lang.org/jmx.html for more information about their usage.

- `otel.queryJmx(javax.management.ObjectName objectName)`
   - This helper has the same functionality as its other signature, but takes an `ObjectName`
   instance if constructing raw name is undesired.

### OpenTelemetry Instrument Helpers

- `otel.doubleCounter(String name, String description, String unit, Map<String, String> labels)`

- `otel.longCounter(String name, String description, String unit, Map<String, String> labels)`

- `otel.doubleUpDownCounter(String name, String description, String unit, Map<String, String> labels)`

- `otel.longUpDownCounter(String name, String description, String unit, Map<String, String> labels)`

- `otel.doubleValueRecorder(String name, String description, String unit, Map<String, String> labels)`

- `otel.longValueRecorder(String name, String description, String unit, Map<String, String> labels)`

These methods will return a new or previously registered instance of the applicable metric
instruments.  Each one provides three additional signatures  where labels, unit, and description
aren't desired upon invocation.

- `otel.<meterMethod>(String name, String description, String unit)` - `labels` are empty map.

- `otel.<meterMethod>(String name, String description)` - `unit` is "1" and `labels` are empty map.

- `otel.<meterMethod>(String name)` - `description` is empty string, `unit` is "1" and `labels` are empty map.

### Compatibility

This metric extension supports Java 7+, though SASL is only supported where
`com.sun.security.sasl.Provider` is available.

### Configuration

| Config option | Required | Type | Description |
| ------------- | -------- | ---- | ----------- |
| `serviceUrl` | **yes** | `string` | The service URL for the JMX RMI/JMXMP endpoint (generally of the form `service:jmx:rmi:///jndi/rmi://<host>:<port>/jmxrmi` or `service:jmx:jmxmp://<host>:<port>`).|
| `groovyScript` | **yes** | `string` | The path for the desired Groovy script. |
| `intervalSeconds` | no | `int` | How often, in seconds, the Groovy script should be run and its resulting metrics exported. 10 by default. |
| `exporterType` | no | `string` | The type of `io.opentelemetry.sdk.metrics.export.MetricExporter` to use: (`otlp`, `inmemory`, `logging`).  `logging` by default. |
| `exporterEndpoint` | no | `string` | The exporter endpoint to use.  Required for `otlp`. |
| `username` | no | `string` | Username for JMX authentication, if applicable. |
| `password` | no | `string` | Password for JMX authentication, if applicable. |
| `keyStorePath` | no | `string` | The key store path is required if client authentication is enabled on the target JVM. |
| `keyStorePassword` | no | `string` | The key store file password if required. |
| `keyStoreType` | no | `string` | The key store type. |
| `trustStorePath` | no | `string` | The trusted store path if the TLS profile is required. |
| `trustStorePassword` | no | `string` | The trust store file password if required. |
| `jmxRemoteProfiles` | no | `string` | Supported JMX remote profiles are TLS in combination with SASL profiles: SASL/PLAIN, SASL/DIGEST-MD5 and SASL/CRAM-MD5. Thus valid `jmxRemoteProfiles` values are: `SASL/PLAIN`, `SASL/DIGEST-MD5`, `SASL/CRAM-MD5`, `TLS SASL/PLAIN`, `TLS SASL/DIGEST-MD5` and `TLS SASL/CRAM-MD5`. |
| `realm` | no | `string` | The realm is required by profile SASL/DIGEST-MD5. |
