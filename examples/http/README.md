# HTTP Example

This is a simple example that demonstrates how to use the OpenTelemetry SDK 
to instrument a simple HTTP based Client/Server application. 
The example creates the **Root Span** on the client and sends the context
over the HTTP request. On the server side, the example shows how to extract the context
and create a **Child Span** with attached a **Span Event**. 

# How to run

## Prerequisites
* Java 1.8.231
* Be on the project root folder

## 1 - Compile 
```bash
gradlew fatJar
```

## 2 - Start the Server
```bash
java -cp examples/build/libs/opentelemetry-examples-0.3.0-SNAPSHOT-all.jar io.opentelemetry.example.http.HttpServer
```
 
## 3 - Start the Client
```bash
java -cp examples/build/libs/opentelemetry-examples-0.3.0-SNAPSHOT-all.jar io.opentelemetry.example.http.HttpClient
```