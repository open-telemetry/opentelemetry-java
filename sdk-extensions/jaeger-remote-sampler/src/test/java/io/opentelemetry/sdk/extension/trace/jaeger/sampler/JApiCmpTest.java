/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import com.google.protobuf.Descriptors;
import io.opentelemetry.sdk.extension.trace.jaeger.proto.api_v2.Sampling;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JApiCmpTest {

  // TODO: delete before merging
  @Test
  @SuppressWarnings({"ModifiedButNotUsed", "ReturnValueIgnored"})
  @Disabled
  void compatibility() {
    Sampling.OperationSamplingStrategy.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.OperationSamplingStrategy.newBuilder().clearField((Descriptors.FieldDescriptor) null);
    Sampling.OperationSamplingStrategy.newBuilder().clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.OperationSamplingStrategy.newBuilder().clone();
    Sampling.OperationSamplingStrategy.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.OperationSamplingStrategy.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);

    Sampling.PerOperationSamplingStrategies.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.PerOperationSamplingStrategies.newBuilder()
        .clearField((Descriptors.FieldDescriptor) null);
    Sampling.PerOperationSamplingStrategies.newBuilder()
        .clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.PerOperationSamplingStrategies.newBuilder().clone();
    Sampling.PerOperationSamplingStrategies.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.PerOperationSamplingStrategies.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);

    Sampling.ProbabilisticSamplingStrategy.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.ProbabilisticSamplingStrategy.newBuilder()
        .clearField((Descriptors.FieldDescriptor) null);
    Sampling.ProbabilisticSamplingStrategy.newBuilder()
        .clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.ProbabilisticSamplingStrategy.newBuilder().clone();
    Sampling.ProbabilisticSamplingStrategy.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.ProbabilisticSamplingStrategy.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);

    Sampling.RateLimitingSamplingStrategy.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.RateLimitingSamplingStrategy.newBuilder()
        .clearField((Descriptors.FieldDescriptor) null);
    Sampling.RateLimitingSamplingStrategy.newBuilder()
        .clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.RateLimitingSamplingStrategy.newBuilder().clone();
    Sampling.RateLimitingSamplingStrategy.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.RateLimitingSamplingStrategy.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);

    Sampling.SamplingStrategyParameters.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.SamplingStrategyParameters.newBuilder().clearField((Descriptors.FieldDescriptor) null);
    Sampling.SamplingStrategyParameters.newBuilder().clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.SamplingStrategyParameters.newBuilder().clone();
    Sampling.SamplingStrategyParameters.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.SamplingStrategyParameters.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);

    Sampling.SamplingStrategyResponse.newBuilder()
        .addRepeatedField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.SamplingStrategyResponse.newBuilder().clearField((Descriptors.FieldDescriptor) null);
    Sampling.SamplingStrategyResponse.newBuilder().clearOneof((Descriptors.OneofDescriptor) null);
    Sampling.SamplingStrategyResponse.newBuilder().clone();
    Sampling.SamplingStrategyResponse.newBuilder()
        .setField((Descriptors.FieldDescriptor) null, (Object) null);
    Sampling.SamplingStrategyResponse.newBuilder()
        .setRepeatedField((Descriptors.FieldDescriptor) null, 0, (Object) null);
  }
}
