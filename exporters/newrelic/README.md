# OpenTelemetry - New Relic Exporter For Spans

A New Relic exporter for sending Open Telemetry spans to New Relic using the New Relic Java Telemetry SDK. For details on how Open Telemetry spans are mapped to New Relic spans, please visit the exporter specs documentation repo. 

# How To Use

`build.gradle`

```
compile("com.newrelic.telemetry:telemetry-http-okhttp:0.3.1")
compile("com.newrelic.telemetry:telemetry-core:0.3.1")
```

or if you're using kotlin build gradle...

`build.gradle.kts`

```
implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.3.1")
implementation("com.newrelic.telemetry:telemetry-core:0.3.1")
```

##### Remove OkHttp

You can remove the dependency on telemetry-http-okhttp, but you will need to construct a MetricBatchSender instance using its builder and provide your own implementation of the com.newrelic.telemetry.http.HttpPoster interface.

`MetricBatchSender sender = MetricBatchSender.builder().httpPoster(<your implementation>);`

Note: to use the sample code below, you will need the telemetry-http-okhttp library mentioned above. It provides implementations communicating via HTTP using the okhttp libraries, respectively.

## Create and configure the tracer for New Relic

```

public Tracer buildNewRelicEnabledTracer(String newRelicInsertKey) {
    SpanExporter spanExporter = NewRelicSpanExporter.newBuilder().apiKey(newRelicInsertKey).build(); 
    SpanProcessor spanProcessor = BatchSampledSpansProcessor.newBuilder(exporter).build();
    TracerSdk tracerSdk = new TracerSdk();
    tracerSdk.addSpanProcessor(spanProcessor);
    return tracerSdk;
  }
```

The NewRelicSpanExporter takes a SpanBatchSender from the New Relic Telemetry SDK. We provide [many more examples](https://github.com/newrelic/newrelic-telemetry-sdk-java/tree/master/telemetry_examples) of how a SpanBatchSender is built and used in the Telemetry SDK. 

## How To Trace 

With the tracer, trace a parent and child method. 

```
public String mySpanMethod(){

  //Create a new parent span for this operation
  Span span = tracer.spanBuilder("my Span Method")
                                .setNoParent()
                                .startSpan();
  
  try {
        return withSpan(span, true, () -> {
          myChildSpanMethod(span);
        });
      } catch (Exception e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }                  
}

public String myChildSpanMethod(Span parentSpan){

 //Create a new span for this child operation
 Span childSpan = tracerSdk.spanBuilder("my Child Span Method")
                               .setParent(parentSpan)
                               .startSpan();
 
 try {
       return withSpan(childSpan, true, () -> {
         //your business logic for myChildMethod goes here
       });
     } catch (Exception e) {
       if (e instanceof RuntimeException) {
         throw (RuntimeException) e;
       }
       throw new RuntimeException(e);
     }                  
}
```