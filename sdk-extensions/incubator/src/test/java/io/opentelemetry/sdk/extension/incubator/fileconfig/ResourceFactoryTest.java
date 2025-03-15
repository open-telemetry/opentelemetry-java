/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.spy;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeNameValueModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalDetectorsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.IncludeExcludeModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ResourceModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceFactoryTest {

  private SpiHelper spiHelper = SpiHelper.create(ResourceFactoryTest.class.getClassLoader());

  @Test
  void create() {
    spiHelper = spy(spiHelper);
    assertThat(
            ResourceFactory.getInstance()
                .create(
                    new ResourceModel()
                        .withAttributes(
                            Arrays.asList(
                                new AttributeNameValueModel()
                                    .withName("service.name")
                                    .withValue("my-service"),
                                new AttributeNameValueModel().withName("key").withValue("val"),
                                new AttributeNameValueModel()
                                    .withName("shape")
                                    .withValue("circle"))),
                    spiHelper,
                    Collections.emptyList()))
        .isEqualTo(
            Resource.getDefault().toBuilder()
                .put("service.name", "my-service")
                .put("key", "val")
                .put("shape", "circle")
                // From ResourceComponentProvider
                .put("color", "red")
                // From ResourceOrderedSecondComponentProvider, which takes priority over
                // ResourceOrderedFirstComponentProvider
                .put("order", "second")
                .build());
  }

  @ParameterizedTest
  @MethodSource("createWithDetectorsArgs")
  void createWithDetectors(
      @Nullable List<String> included, @Nullable List<String> excluded, Resource expectedResource) {
    ResourceModel resourceModel =
        new ResourceModel()
            .withDetectorsDevelopment(
                new ExperimentalDetectorsModel()
                    .withAttributes(
                        new IncludeExcludeModel().withIncluded(included).withExcluded(excluded)));
    Resource resource =
        ResourceFactory.getInstance().create(resourceModel, spiHelper, Collections.emptyList());
    assertThat(resource).isEqualTo(expectedResource);
  }

  private static Stream<Arguments> createWithDetectorsArgs() {
    return Stream.of(
        Arguments.of(
            null,
            null,
            Resource.getDefault().toBuilder()
                .put("color", "red")
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.of(
            Collections.singletonList("color"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.of(
            Arrays.asList("color", "shape"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").put("shape", "square").build()),
        Arguments.of(
            null,
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.of(
            null,
            Arrays.asList("color", "shape"),
            Resource.getDefault().toBuilder().put("order", "second").build()),
        Arguments.of(
            Collections.singletonList("color"),
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder().build()),
        Arguments.of(
            Arrays.asList("color", "shape"),
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder().put("shape", "square").build()),
        Arguments.of(
            Collections.singletonList("c*"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.of(
            Collections.singletonList("c?lor"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.of(
            null,
            Collections.singletonList("c*"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.of(
            null,
            Collections.singletonList("c?lor"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.of(
            Collections.singletonList("*o*"),
            Collections.singletonList("order"),
            Resource.getDefault().toBuilder().put("color", "red").build()));
  }
}
