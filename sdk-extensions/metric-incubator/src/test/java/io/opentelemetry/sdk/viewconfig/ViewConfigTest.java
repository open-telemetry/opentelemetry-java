/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistryBuilder;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class ViewConfigTest {

  @Test
  void registerViews_FullConfig() {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();

    ViewConfig.registerViews(
        builder, new File(ViewConfigTest.class.getResource("/full-config.yaml").getFile()));

    assertThat(builder)
        .extracting(
            "viewRegistryBuilder", as(InstanceOfAssertFactories.type(ViewRegistryBuilder.class)))
        .extracting("orderedViews", as(InstanceOfAssertFactories.list(Object.class)))
        .hasSize(2);
  }

  @Test
  void loadViewConfig_FullConfig() {
    List<ViewConfigSpecification> viewConfigSpecs =
        ViewConfig.loadViewConfig(
            new File(ViewConfigTest.class.getResource("/full-config.yaml").getFile()));

    assertThat(viewConfigSpecs)
        .hasSize(2)
        .satisfiesExactly(
            viewConfigSpec -> {
              SelectorSpecification selectorSpec = viewConfigSpec.getSelectorSpecification();
              assertThat(selectorSpec.getInstrumentName()).isEqualTo("name1");
              assertThat(selectorSpec.getInstrumentType()).isEqualTo(InstrumentType.COUNTER);
              assertThat(selectorSpec.getMeterName()).isEqualTo("meterName1");
              assertThat(selectorSpec.getMeterVersion()).isEqualTo("1.0.0");
              assertThat(selectorSpec.getMeterSchemaUrl()).isEqualTo("http://example1.com");
              ViewSpecification viewSpec = viewConfigSpec.getViewSpecification();
              assertThat(viewSpec.getName()).isEqualTo("name1");
              assertThat(viewSpec.getDescription()).isEqualTo("description1");
              assertThat(viewSpec.getAggregation()).isEqualTo("sum");
              assertThat(viewSpec.getAttributeKeys()).isEqualTo(Arrays.asList("foo", "bar"));
            },
            viewConfigSpec -> {
              SelectorSpecification selectorSpec = viewConfigSpec.getSelectorSpecification();
              assertThat(selectorSpec.getInstrumentName()).isEqualTo("name2");
              assertThat(selectorSpec.getInstrumentType()).isEqualTo(InstrumentType.COUNTER);
              assertThat(selectorSpec.getMeterName()).isEqualTo("meterName2");
              assertThat(selectorSpec.getMeterVersion()).isEqualTo("2.0.0");
              assertThat(selectorSpec.getMeterSchemaUrl()).isEqualTo("http://example2.com");
              ViewSpecification viewSpec = viewConfigSpec.getViewSpecification();
              assertThat(viewSpec.getName()).isEqualTo("name2");
              assertThat(viewSpec.getDescription()).isEqualTo("description2");
              assertThat(viewSpec.getAggregation()).isEqualTo("last_value");
              assertThat(viewSpec.getAttributeKeys()).isEqualTo(Arrays.asList("baz", "qux"));
            });
  }

  @Test
  void loadViewConfig_Invalid() {
    assertThatThrownBy(() -> ViewConfig.loadViewConfig(new File("/" + UUID.randomUUID())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("An error occurred reading view config file:");

    assertThatThrownBy(
            () ->
                ViewConfig.loadViewConfig(
                    new File(
                        ViewConfigTest.class.getResource("/empty-view-config.yaml").getFile())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Failed to parse view config file")
        .hasRootCauseMessage("view is required");
    assertThatThrownBy(
            () ->
                ViewConfig.loadViewConfig(
                    new File(
                        ViewConfigTest.class.getResource("/empty-selector-config.yaml").getFile())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Failed to parse view config file")
        .hasRootCauseMessage("selector is required");
  }

  @Test
  void toView_Empty() {
    View view = ViewConfig.toView(ViewSpecification.builder().build());
    assertThat(view).isEqualTo(View.builder().build());
  }

  @Test
  void toView() {
    View view =
        ViewConfig.toView(
            ViewSpecification.builder()
                .name("name")
                .description("description")
                .aggregation("sum")
                .attributeKeys(Arrays.asList("foo", "bar"))
                .build());
    assertThat(view.getName()).isEqualTo("name");
    assertThat(view.getDescription()).isEqualTo("description");
    assertThat(view.getAggregation()).isEqualTo(Aggregation.sum());
    assertThat(
            view.getAttributesProcessor()
                .process(
                    Attributes.builder()
                        .put("foo", "val")
                        .put("bar", "val")
                        .put("baz", "val")
                        .build(),
                    Context.current()))
        .containsEntry("foo", "val")
        .containsEntry("bar", "val")
        .satisfies(
            (Consumer<Attributes>)
                attributes -> assertThat(attributes.get(AttributeKey.stringKey("baz"))).isBlank());
  }

  @Test
  void toAggregation() {
    assertThat(ViewConfig.toAggregation("sum")).isEqualTo(Aggregation.sum());
    assertThat(ViewConfig.toAggregation("last_value")).isEqualTo(Aggregation.lastValue());
    assertThat(ViewConfig.toAggregation("histogram")).isEqualTo(Aggregation.histogram());
    assertThat(ViewConfig.toAggregation("drop")).isEqualTo(Aggregation.drop());
    assertThatThrownBy(() -> ViewConfig.toAggregation("foo"))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized aggregation foo");
  }

  @Test
  void toInstrumentSelector_Empty() {
    InstrumentSelector selector =
        ViewConfig.toInstrumentSelector(SelectorSpecification.builder().build());
    assertThat(selector).isEqualTo(InstrumentSelector.builder().build());
  }

  @Test
  void toInstrumentSelector() {
    InstrumentSelector selector =
        ViewConfig.toInstrumentSelector(
            SelectorSpecification.builder()
                .instrumentName("name")
                .instrumentType(InstrumentType.COUNTER)
                .meterName("meterName")
                .meterVersion("meterVersion")
                .meterSchemaUrl("http://example.com")
                .build());

    assertThat(selector.getInstrumentNameFilter().test("name")).isTrue();
    assertThat(selector.getInstrumentNameFilter().test("name1")).isFalse();
    assertThat(selector.getInstrumentType()).isEqualTo(InstrumentType.COUNTER);
    assertThat(selector.getMeterSelector().getNameFilter().test("meterName")).isTrue();
    assertThat(selector.getMeterSelector().getNameFilter().test("meterName1")).isFalse();
    assertThat(selector.getMeterSelector().getVersionFilter().test("meterVersion")).isTrue();
    assertThat(selector.getMeterSelector().getVersionFilter().test("meterVersion1")).isFalse();
    assertThat(selector.getMeterSelector().getSchemaUrlFilter().test("http://example.com"))
        .isTrue();
    assertThat(selector.getMeterSelector().getSchemaUrlFilter().test("http://example1.com"))
        .isFalse();
  }
}
