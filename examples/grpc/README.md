# gRPC Example

This is a simple example that demonstrates how to use the OpenTelemetry SDK 
to instrument normal and streamed gRPC calls. 
The example creates the **Root Span** on the client and sends the distributed context
over the gRPC request. On the server side, the example shows how to extract the context
and create a **Child Span**. 

# How to run

## Prerequisites
* Java 1.8.231

## 1 - Compile 
```bash
gradlew fatJar
```

## 2 - Start the Server
```bash
java -cp .\build\libs\opentelemetry-example-grpc-all-0.2.0.jar io.opentelemetry.example.HelloWorldServer
```
 
## 3 - Start the Client
```bash
java -cp .\build\libs\opentelemetry-example-grpc-all-0.2.0.jar io.opentelemetry.example.HelloWorldClient
```