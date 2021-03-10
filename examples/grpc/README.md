# gRPC Example

**Note:** This is an advanced scenario useful for people that want to *manually* instrument their own code. 

This example demonstrates how to use the OpenTelemetry API to instrument normal and streamed gRPC calls. 
The example creates the **Root Span** on the client and sends the distributed context
over the gRPC request. On the server side, the example shows how to extract the context
and create a **Child Span**. 

# How to run

## Prerequisites
* Java 1.8

## 1 - Compile 
```shell script
../gradlew shadowJar
```

## 2 - Start the Server
```shell script
java -cp ./build/libs/opentelemetry-examples-grpc-0.1.0-SNAPSHOT-all.jar io.opentelemetry.example.grpc.HelloWorldServer
```
 
## 3 - Start the normal Client
```shell script
java -cp ./build/libs/opentelemetry-examples-grpc-0.1.0-SNAPSHOT-all.jar io.opentelemetry.example.grpc.HelloWorldClient
```

## 4 - Start the streamed Client
```shell script
java -cp ./build/libs/opentelemetry-examples-grpc-all-0.1.0-SNAPSHOT.jar io.opentelemetry.example.grpc.HelloWorldClientStream
```