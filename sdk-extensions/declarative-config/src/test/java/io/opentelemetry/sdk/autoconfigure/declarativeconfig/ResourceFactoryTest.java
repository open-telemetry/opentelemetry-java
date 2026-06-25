/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AttributeNameValueModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalResourceDetectionModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalResourceDetectorModel;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourceFactoryTest {

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(ComponentLoader.forClassLoader(getClass().getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArgs")
  void create(ResourceModel model, Resource expectedResource) {
    assertThat(ResourceFactory.getInstance().create(model, context)).isEqualTo(expectedResource);
  }

  private static Stream<Arguments> createArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "with attributes",
            new ResourceModel()
                .withAttributes(
                    Arrays.asList(
                        new AttributeNameValueModel()
                            .withName("service.name")
                            .withValue("my-service"),
                        new AttributeNameValueModel().withName("key").withValue("val"),
                        new AttributeNameValueModel().withName("shape").withValue("circle"))),
            Resource.getDefault().toBuilder()
                .put("shape", "circle")
                .put("service.name", "my-service")
                .put("key", "val")
                .build()),
        Arguments.argumentSet(
            "with schema url",
            new ResourceModel().withSchemaUrl("http://foo"),
            Resource.getDefault().toBuilder().setSchemaUrl("http://foo").build()),
        Arguments.argumentSet(
            "with attributes list",
            new ResourceModel().withAttributesList("key1=val1,key2=val2"),
            Resource.getDefault().toBuilder().put("key1", "val1").put("key2", "val2").build()));
  }

  @ParameterizedTest
  @MethodSource("createWithDetectorsArgs")
  void createWithDetectors(
      @Nullable List<String> included, @Nullable List<String> excluded, Resource expectedResource) {
    ResourceModel resourceModel =
        new ResourceModel()
            .withDetectionDevelopment(
                new ExperimentalResourceDetectionModel()
                    .withDetectors(
                        Arrays.asList(
                            new ExperimentalResourceDetectorModel()
                                .withAdditionalProperty("order_first", null),
                            new ExperimentalResourceDetectorModel()
                                .withAdditionalProperty("order_second", null),
                            new ExperimentalResourceDetectorModel()
                                .withAdditionalProperty("shape_color", null)))
                    .withAttributes(
                        new IncludeExcludeModel().withIncluded(included).withExcluded(excluded)));
    Resource resource = ResourceFactory.getInstance().create(resourceModel, context);
    assertThat(resource).isEqualTo(expectedResource);
  }

  private static Stream<Arguments> createWithDetectorsArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "no filters",
            null,
            null,
            Resource.getDefault().toBuilder()
                .put("color", "red")
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.argumentSet(
            "include color",
            Collections.singletonList("color"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.argumentSet(
            "include color shape",
            Arrays.asList("color", "shape"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").put("shape", "square").build()),
        Arguments.argumentSet(
            "exclude color",
            null,
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.argumentSet(
            "exclude color shape",
            null,
            Arrays.asList("color", "shape"),
            Resource.getDefault().toBuilder().put("order", "second").build()),
        Arguments.argumentSet(
            "include and exclude same",
            Collections.singletonList("color"),
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder().build()),
        Arguments.argumentSet(
            "include color shape exclude color",
            Arrays.asList("color", "shape"),
            Collections.singletonList("color"),
            Resource.getDefault().toBuilder().put("shape", "square").build()),
        Arguments.argumentSet(
            "include c* glob",
            Collections.singletonList("c*"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.argumentSet(
            "include c?lor glob",
            Collections.singletonList("c?lor"),
            null,
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.argumentSet(
            "exclude c* glob",
            null,
            Collections.singletonList("c*"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.argumentSet(
            "exclude c?lor glob",
            null,
            Collections.singletonList("c?lor"),
            Resource.getDefault().toBuilder()
                .put("shape", "square")
                .put("order", "second")
                .build()),
        Arguments.argumentSet(
            "include *o* exclude order",
            Collections.singletonList("*o*"),
            Collections.singletonList("order"),
            Resource.getDefault().toBuilder().put("color", "red").build()),
        Arguments.argumentSet(
            "exclude order",
            null,
            Collections.singletonList("order"),
            Resource.getDefault().toBuilder().put("color", "red").put("shape", "square").build()));
  }

  @ParameterizedTest
  @MethodSource("createInvalidDetectorsArgs")
  void createWithDetectors_Invalid(ResourceModel model, String expectedMessage) {
    assertThatThrownBy(() -> ResourceFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  private static Stream<Arguments> createInvalidDetectorsArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "unknown detector",
            new ResourceModel()
                .withDetectionDevelopment(
                    new ExperimentalResourceDetectionModel()
                        .withDetectors(
                            Collections.singletonList(
                                new ExperimentalResourceDetectorModel()
                                    .withAdditionalProperty("foo", null)))),
            "No component provider detected for io.opentelemetry.sdk.resources.Resource with name \"foo\"."),
        Arguments.argumentSet(
            "detector multiple entries",
            new ResourceModel()
                .withDetectionDevelopment(
                    new ExperimentalResourceDetectionModel()
                        .withDetectors(
                            Collections.singletonList(
                                new ExperimentalResourceDetectorModel()
                                    .withAdditionalProperty("foo", null)
                                    .withAdditionalProperty("bar", null)))),
            "resource detector must have exactly one entry but has 2: [foo,bar]"),
        Arguments.argumentSet(
            "detector no entries",
            new ResourceModel()
                .withDetectionDevelopment(
                    new ExperimentalResourceDetectionModel()
                        .withDetectors(
                            Collections.singletonList(new ExperimentalResourceDetectorModel()))),
            "resource detector must have exactly one entry but has 0"),
        Arguments.argumentSet(
            "included empty list",
            new ResourceModel()
                .withDetectionDevelopment(
                    new ExperimentalResourceDetectionModel()
                        .withAttributes(
                            new IncludeExcludeModel()
                                .withIncluded(Collections.emptyList())
                                .withExcluded(null))),
            "included must not be empty"),
        Arguments.argumentSet(
            "excluded empty list",
            new ResourceModel()
                .withDetectionDevelopment(
                    new ExperimentalResourceDetectionModel()
                        .withAttributes(
                            new IncludeExcludeModel()
                                .withIncluded(null)
                                .withExcluded(Collections.emptyList()))),
            "excluded must not be empty"));
  }
}
