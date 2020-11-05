# OpenTelemetry SDK Extension zPages

[![Javadocs][javadoc-image]][javadoc-url]

This module contains code for the OpenTelemetry Java zPages, which are a collection of dynamic HTML
web pages embedded in your app that display stats and trace data. Learn more in [this blog post][zPages blog];

* Java 8 compatible.

<!--- TODO: Update javadoc -->
[javadoc-image]: https://www.javadoc.io/badge/io.opentelemetry/opentelemetry-sdk-contrib-auto-config.svg
[javadoc-url]: https://www.javadoc.io/doc/io.opentelemetry/opentelemetry-sdk-contrib-auto-config
[zPages blog]: https://medium.com/opentelemetry/zpages-in-opentelemetry-2b080a81eb47

## Quickstart

### Add the dependencies to your project

For Maven, add the following to your `pom.xml`:
```xml
<dependencies>
  <dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk-extension-zpages</artifactId>
    <version>0.10.0</version>
  </dependency>
</dependencies>
```

For Gradle, add the following to your dependencies:
```groovy
implementation 'io.opentelemetry:opentelemetry-sdk-extension-zpages:0.10.0'
```

### Register the zPages

**Note:** The package `com.sun.net.httpserver` is required to use the default zPages setup. Please make sure your
version of the JDK includes this package.

To set-up the zPages, simply call `ZPageServer.startHttpServerAndRegisterAllPages(int port)` in your
main function:

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    ZPageServer.startHttpServerAndRegisterAllPages(8080);
    // ... do work
  }
}
```

Alternatively, you can call `ZPageServer.registerAllPagesToHttpServer(HttpServer server)` to
register the zPages to a shared server:

```java
public class MyMainClass {
  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
    ZPageServer.registerAllPagesToHttpServer(server);
    server.start();
    // ... do work
  }
}
```

### Access the zPages

#### View all available zPages on the `/` index page

The index page `/` lists all available zPages with a link and description.

#### View trace spans on the `/tracez` zPage

The /tracez zPage displays information on running spans, sample span latencies, and sample error
spans. The data is aggregated into a summary-level table:

![tracez-table](img/tracez-table.png)

You can click on each of the counts in the table cells to access the corresponding span
details. For example, here are the details of the `ChildSpan` latency sample (row 1, col 4):

![tracez-details](img/tracez-details.png)

#### View and update the tracing configuration on the `/traceconfigz` zPage

The /traceconfigz zPage displays information about the currently active tracing configuration and 
provides an interface for users to modify relevant parameters. Here is what the web page looks like:

![traceconfigz](img/traceconfigz.png)

## Benchmark Testing

This module contains two sets of benchmark tests: one for adding spans to an instance of
TracezSpanBuckets and another for retrieving counts and spans with TracezDataAggregator. You can run
the tests yourself with the following commands:

```
./gradlew -PjmhIncludeSingleClass=TracezSpanBucketsBenchmark clean :opentelemetry-sdk-extension-zpages:jmh
./gradlew -PjmhIncludeSingleClass=TracezDataAggregatorBenchmark clean :opentelemetry-sdk-extension-zpages:jmh
```
