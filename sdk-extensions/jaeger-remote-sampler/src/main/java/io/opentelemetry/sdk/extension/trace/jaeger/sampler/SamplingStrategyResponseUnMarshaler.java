/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.internal.marshal.CodedInputStream;
import java.io.IOException;

class SamplingStrategyResponseUnMarshaler {

  private SamplingStrategyResponseUnMarshaler() {}

  static SamplingStrategyResponse read(byte[] payload) throws IOException {
    SamplingStrategyResponse.Builder responseBuilder = new SamplingStrategyResponse.Builder();
    CodedInputStream codedInputStream = CodedInputStream.newInstance(payload);
    parseResponse(responseBuilder, codedInputStream);
    return responseBuilder.build();
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
          int length = input.readRawVarint32();
          responseBuilder.setProbabilisticSamplingStrategy(parseProbabilistic(input, length));
          break;
        case 26:
          input.readRawVarint32(); // skip length
          responseBuilder.setRateLimitingSamplingStrategy(parseRateLimiting(input));
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
      CodedInputStream input, int length) throws IOException {
    SamplingStrategyResponse.ProbabilisticSamplingStrategy.Builder builder =
        new SamplingStrategyResponse.ProbabilisticSamplingStrategy.Builder();
    if (length == 0) {
      // Default probabilistic strategy.
      return builder.setSamplingRate(0.0).build();
    }
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
          int length = input.readRawVarint32();
          builder.setProbabilisticSamplingStrategy(parseProbabilistic(input, length));
          break;
        default:
          input.skipField(tag);
          break;
      }

      if (operationParsed && probabilisticSamplingParsed) {
        break;
      }
    }
    return builder.build();
  }
}
