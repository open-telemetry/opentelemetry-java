/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.CodedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"SystemOut", "DefaultCharset"})
class SamplingStrategyResponseUnMarshaller extends UnMarshaller {

  private SamplingStrategyResponse samplingStrategyResponse =
      new SamplingStrategyResponse.Builder().build();

  public SamplingStrategyResponse get() {
    return samplingStrategyResponse;
  }

  @Override
  public void read(InputStream inputStream) throws IOException {
    byte[] bytes = readAllBytes(inputStream);
    SamplingStrategyResponse.Builder responseBuilder = new SamplingStrategyResponse.Builder();
    try {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(bytes);
      parseResponse(responseBuilder, codedInputStream);
    } catch (IOException ex) {
      // use empty/default message
    }
    samplingStrategyResponse = responseBuilder.build();
  }

  private static void parseResponse(
      SamplingStrategyResponse.Builder responseBuilder, CodedInputStream input) throws IOException {
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 8:
          parseSamplingStrategyType(responseBuilder, input);
          break;
        case 18:
          input.readRawVarint32(); // skip length
          responseBuilder.setProbabilisticSamplingStrategy(parseProbabilistic(input));
          break;
        case 26:
          input.readRawVarint32(); // skip length
          responseBuilder.setRateLimitingSamplingStrategy(parseRateLimiting(input));
          parseRateLimiting(input);
          break;
        case 34:
          input.readRawVarint32(); // skip length
          responseBuilder.setPerOperationSamplingStrategies(parsePerOperationStrategy(input));
          break;
        default:
          input.skipField(tag);
      }
    }
  }

  private static void parseSamplingStrategyType(
      SamplingStrategyResponse.Builder responseBuilder, CodedInputStream input) throws IOException {
    int tagValue = input.readRawVarint32();
    switch (tagValue) {
      case 0:
        responseBuilder.setSamplingStrategyType(
            SamplingStrategyResponse.SamplingStrategyType.PROBABILISTIC);
        break;
      case 1:
        responseBuilder.setSamplingStrategyType(
            SamplingStrategyResponse.SamplingStrategyType.RATE_LIMITING);
        break;
      default:
        responseBuilder.setSamplingStrategyType(
            SamplingStrategyResponse.SamplingStrategyType.UNRECOGNIZED);
        break;
    }
  }

  private static SamplingStrategyResponse.ProbabilisticSamplingStrategy parseProbabilistic(
      CodedInputStream input) throws IOException {
    SamplingStrategyResponse.ProbabilisticSamplingStrategy.Builder builder =
        new SamplingStrategyResponse.ProbabilisticSamplingStrategy.Builder();
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 9:
          double samplingRate = input.readDouble();
          return builder.setSamplingRate(samplingRate).build();
        default:
          input.skipField(tag);
          break;
      }
    }
    return builder.build();
  }

  private static SamplingStrategyResponse.RateLimitingSamplingStrategy parseRateLimiting(
      CodedInputStream input) throws IOException {
    SamplingStrategyResponse.RateLimitingSamplingStrategy.Builder builder =
        new SamplingStrategyResponse.RateLimitingSamplingStrategy.Builder();
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 8:
          int rate = input.readRawVarint32();
          return builder.setMaxTracesPerSecond(rate).build();
        default:
          input.skipField(tag);
          break;
      }
    }
    return builder.build();
  }

  private static SamplingStrategyResponse.PerOperationSamplingStrategies parsePerOperationStrategy(
      CodedInputStream input) throws IOException {
    SamplingStrategyResponse.PerOperationSamplingStrategies.Builder builder =
        new SamplingStrategyResponse.PerOperationSamplingStrategies.Builder();
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 9:
          double defaultProbability = input.readDouble();
          builder.setDefaultSamplingProbability(defaultProbability);
          break;
        case 17:
          double lowerBoundPerSecond = input.readDouble();
          builder.setDefaultLowerBoundTracesPerSecond(lowerBoundPerSecond);
          break;
        case 26:
          input.readRawVarint32(); // skip length
          SamplingStrategyResponse.OperationSamplingStrategy strategy =
              parseOperationStrategy(input);
          if (strategy != null) {
            builder.addOperationStrategy(strategy);
          }
          break;
        case 33:
          double upperBoundPerSecond = input.readDouble();
          builder.setDefaultUpperBoundTracesPerSecond(upperBoundPerSecond);
          break;
        default:
          input.skipField(tag);
          break;
      }
    }
    return builder.build();
  }

  private static SamplingStrategyResponse.OperationSamplingStrategy parseOperationStrategy(
      CodedInputStream input) throws IOException {

    SamplingStrategyResponse.OperationSamplingStrategy.Builder builder =
        new SamplingStrategyResponse.OperationSamplingStrategy.Builder();

    boolean done = false;
    boolean operationParsed = false;
    boolean probabilisticSamplingParsed = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 10:
          operationParsed = true;
          String operation = input.readStringRequireUtf8();
          builder.setOperation(operation);
          break;
        case 18:
          probabilisticSamplingParsed = true;
          input.readRawVarint32(); // skip length
          builder.setProbabilisticSamplingStrategy(parseProbabilistic(input));
          break;
        default:
          input.skipField(tag);
          break;
      }

      if (!operationParsed && probabilisticSamplingParsed) {
        break;
      }
    }
    return builder.build();
  }

  private static byte[] readAllBytes(InputStream inputStream) throws IOException {
    final int bufLen = 4 * 0x400; // 4KB
    byte[] buf = new byte[bufLen];
    int readLen;
    IOException exception = null;

    try {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        while ((readLen = inputStream.read(buf, 0, bufLen)) != -1) {
          outputStream.write(buf, 0, readLen);
        }
        return outputStream.toByteArray();
      }
    } catch (IOException e) {
      exception = e;
      throw e;
    } finally {
      if (exception == null) {
        inputStream.close();
      } else {
        try {
          inputStream.close();
        } catch (IOException e) {
          exception.addSuppressed(e);
        }
      }
    }
  }
}
