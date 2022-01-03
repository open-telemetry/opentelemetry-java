/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.CodedInputStream;
import io.opentelemetry.exporter.otlp.internal.UnMarshaller;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;

@SuppressWarnings({"SystemOut","DefaultCharset"})
class SamplingStrategyResponseUnMarshaller extends UnMarshaller {

  @Nullable
  private Sampling.SamplingStrategyResponse samplingStrategyResponse;

  @Nullable
  public Sampling.SamplingStrategyResponse get() {
    return samplingStrategyResponse;
  }

  @Override
  public void read(InputStream inputStream) throws IOException {
    byte[] bytes = readAllBytes(inputStream);
    Sampling.SamplingStrategyResponse.Builder responseBuilder = Sampling.SamplingStrategyResponse.newBuilder();
    try {
      CodedInputStream codedInputStream = CodedInputStream.newInstance(bytes);
      parseResponse(responseBuilder, codedInputStream);
    } catch (IOException ex) {
      // use empty/default message
    }
    samplingStrategyResponse = responseBuilder.build();
  }

  private static void parseResponse(Sampling.SamplingStrategyResponse.Builder responseBuilder, CodedInputStream input)
      throws IOException {
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
          input.readRawVarint32();  // skip length
          parseProbabilistic(responseBuilder.getProbabilisticSamplingBuilder(), input);
          break;
        case 26:
          input.readRawVarint32();  // skip length
          parseRateLimiting(responseBuilder.getRateLimitingSamplingBuilder(), input);
          break;
        case 34:
          input.readRawVarint32();  // skip length
          parsePerOperationStrategy(responseBuilder.getOperationSamplingBuilder(), input);
          break;
        default:
          input.skipField(tag);
      }
    }
  }

  private static void parseSamplingStrategyType(Sampling.SamplingStrategyResponse.Builder responseBuilder, CodedInputStream input)
      throws IOException {
    int tagValue = input.readRawVarint32();
    switch (tagValue) {
      case 0:
        responseBuilder.setStrategyType(Sampling.SamplingStrategyType.PROBABILISTIC);
        break;
      case 1:
        responseBuilder.setStrategyType(Sampling.SamplingStrategyType.RATE_LIMITING);
        break;
      default:
        break;
    }
  }

  private static void parseProbabilistic(Sampling.ProbabilisticSamplingStrategy.Builder probabilisticBuilder, CodedInputStream input)
      throws IOException {
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 9:
          double samplingRate = input.readDouble();
          probabilisticBuilder.setSamplingRate(samplingRate);
          return;
        default:
          input.skipField(tag);
          break;
      }
    }
  }

  private static void parseRateLimiting(Sampling.RateLimitingSamplingStrategy.Builder ratelimitingBuilder, CodedInputStream input)
      throws IOException {
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 8:
          int rate = input.readRawVarint32();
          ratelimitingBuilder.setMaxTracesPerSecond(rate);
          return;
        default:
          input.skipField(tag);
          break;
      }
    }
  }

  private static void parsePerOperationStrategy(Sampling.PerOperationSamplingStrategies.Builder perOperationBuilder, CodedInputStream input)
      throws IOException {
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 9:
          double defaultProbability = input.readDouble();
          perOperationBuilder.setDefaultSamplingProbability(defaultProbability);
          break;
        case 17:
          double lowerBoundPerSecond = input.readDouble();
          perOperationBuilder.setDefaultLowerBoundTracesPerSecond(lowerBoundPerSecond);
          break;
        case 26:
          input.readRawVarint32(); // skip length
          parseOperationStrategy(perOperationBuilder.addPerOperationStrategiesBuilder(), input);
          break;
        case 33:
          double upperBoundPerSecond = input.readDouble();
          perOperationBuilder.setDefaultUpperBoundTracesPerSecond(upperBoundPerSecond);
          break;
        default:
          input.skipField(tag);
          break;
      }
    }
  }

  private static void parseOperationStrategy(Sampling.OperationSamplingStrategy.Builder operationBuilder, CodedInputStream input)
      throws IOException {
    boolean done = false;
    while (!done) {
      if (!operationBuilder.getOperation().isEmpty() && operationBuilder.hasProbabilisticSampling()) {
        return;
      }

      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 10:
          String string = input.readStringRequireUtf8();
          operationBuilder.setOperation(string);
          break;
        case 18:
          input.readRawVarint32(); // skip length
          parseProbabilistic(operationBuilder.getProbabilisticSamplingBuilder(), input);
          break;
        default:
          input.skipField(tag);
          break;
      }
    }
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
      }
      else {
        try {
          inputStream.close();
        } catch (IOException e) {
          exception.addSuppressed(e);
        }
      }
    }
  }
}
