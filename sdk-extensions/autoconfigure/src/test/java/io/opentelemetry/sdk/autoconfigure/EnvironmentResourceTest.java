/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.event.Level.WARN;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EnvironmentResourceTest {

  @RegisterExtension
  LogCapturer logs =
      LogCapturer.create().captureForLogger(EnvironmentResource.class.getName() + "$EntityParser");

  private static Resource parse(String entities) {
    return EnvironmentResource.createEnvironmentResource(
        DefaultConfigProperties.createFromMap(Collections.singletonMap("otel.entities", entities)));
  }

  @Test
  void reservedCharactersAreDecodedInValues() {
    Resource resource = parse("host{host.id=H1}[config=%7B%7D%5B%5D%40%3B%2C%3D,empty=]");
    Entity expected =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.builder().put("config", "{}[]@;,=").put("empty", "").build())
            .build();
    assertThat(EntityUtil.getEntities(resource)).containsExactly(expected);
  }

  @Test
  void emptyDefinitionsAreIgnored() {
    Resource resource = parse(";host{host.id=H1};;;");
    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build());
  }

  @Test
  void emptyDescriptionIsAllowed() {
    assertThat(EntityUtil.getEntities(parse("host{host.id=H1}[]")))
        .containsExactly(Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build());
  }

  @ParameterizedTest
  @MethodSource("malformedDefinitions")
  void malformedDefinitionDoesNotDiscardValidNeighbors(String malformed) {
    Resource resource = parse("host{host.id=H1};" + malformed + ";service{service.name=S1}");
    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).build(),
            Entity.builder("service", Attributes.of(stringKey("service.name"), "S1")).build());
    logs.assertContains(event -> event.getLevel().equals(WARN), "Malformed entity definition");
  }

  @ParameterizedTest
  @MethodSource("malformedDefinitions")
  void malformedDefinitionAtEndOfInput(String malformed) {
    assertThat(parse(malformed)).isEqualTo(Resource.empty());
    logs.assertContains(event -> event.getLevel().equals(WARN), "Malformed entity definition");
  }

  static Stream<Arguments> malformedDefinitions() {
    return Stream.of(
        Arguments.argumentSet("empty type", "{id=1}"),
        Arguments.argumentSet("missing identity", "test{}"),
        Arguments.argumentSet("description before identity", "test[name=broken]{id=1}"),
        Arguments.argumentSet("missing opening brace", "testid=1}"),
        Arguments.argumentSet("missing closing brace", "test{id=1"),
        Arguments.argumentSet("unclosed description", "test{id=1}[name=broken"),
        Arguments.argumentSet("extra identity block", "test{id=1}{id=2}"),
        Arguments.argumentSet("extra description block", "test{id=1}[name=1][name=2]"),
        Arguments.argumentSet("trailing garbage", "test{id=1}garbage"),
        Arguments.argumentSet("missing equals", "test{id}"),
        Arguments.argumentSet("dangling identity key", "test{id=1,dangling}"),
        Arguments.argumentSet("dangling description key", "test{id=1}[name=1,dangling]"),
        Arguments.argumentSet("empty key", "test{=1}"),
        Arguments.argumentSet("trailing comma", "test{id=1,}"),
        Arguments.argumentSet(
            "overlapping identifying/descriptive attribute keys", "test{id=1}[id=2]"),
        Arguments.argumentSet("type starts with digit", "1test{id=1}"),
        Arguments.argumentSet("invalid type character", "test/type{id=1}"),
        Arguments.argumentSet("non-ASCII type", "tést{id=1}"),
        Arguments.argumentSet("invalid key character", "test{invalid/key=1}"),
        Arguments.argumentSet("key starts with digit", "test{1key=1}"),
        Arguments.argumentSet("unencoded equals", "test{id=a=b}"),
        Arguments.argumentSet("unencoded at", "test{id=a@b}"),
        Arguments.argumentSet("unencoded bracket", "test{id=a[b}"),
        Arguments.argumentSet(
            "type too long", String.join("", Collections.nCopies(256, "t")) + "{id=1}"),
        Arguments.argumentSet(
            "key too long", "test{" + String.join("", Collections.nCopies(256, "k")) + "=1}"));
  }

  @ParameterizedTest
  @MethodSource("schemaUrls")
  void schemaUrlValidation(String input, @Nullable String expected) {
    Resource resource = parse("host{host.id=H1}@" + input);
    assertThat(EntityUtil.getEntities(resource)).hasSize(1);
    assertThat(EntityUtil.getEntities(resource).iterator().next().getSchemaUrl())
        .isEqualTo(expected);
    if (expected == null) {
      logs.assertContains(
          event -> event.getLevel().equals(WARN), "Ignoring invalid entity schema URL");
    }
  }

  static Stream<Arguments> schemaUrls() {
    return Stream.of(
        Arguments.argumentSet(
            "absolute URL", "https://example.com/schema", "https://example.com/schema"),
        Arguments.argumentSet(
            "URL delimiters stay in the schema",
            "https://[::1]/schema?a=b,c@d",
            "https://[::1]/schema?a=b,c@d"),
        Arguments.argumentSet(
            "encoded URL retained",
            "https://example.com/schema%20version",
            "https://example.com/schema%20version"),
        Arguments.argumentSet("URN", "urn:example:schema", "urn:example:schema"),
        Arguments.argumentSet("relative URL ignored", "invalid-url", null),
        Arguments.argumentSet("invalid host ignored", "http://[", null),
        Arguments.argumentSet("space ignored", "http://bad host", null),
        Arguments.argumentSet("invalid percent escape ignored", "https://example.com/%ZZ", null),
        Arguments.argumentSet("empty URL ignored", "", null));
  }

  @ParameterizedTest
  @MethodSource("duplicateEntities")
  void duplicateEntityUsesOnlyLastDefinition(String input, Entity expected) {
    Resource resource = parse(input);
    assertThat(EntityUtil.getEntities(resource)).containsExactly(expected);
    assertThat(resource.getAttributes())
        .isEqualTo(
            Attributes.builder()
                .putAll(expected.getId())
                .putAll(expected.getDescription())
                .build());
    logs.assertContains(event -> event.getLevel().equals(WARN), "Duplicate entity type [host]");
  }

  static Stream<Arguments> duplicateEntities() {
    return Stream.of(
        Arguments.argumentSet(
            "identical identity",
            "host{host.id=H1}[host.name=old,host.type=discarded];host{host.id=H1}[host.name=new]",
            Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
                .setDescription(Attributes.of(stringKey("host.name"), "new"))
                .build()),
        Arguments.argumentSet(
            "different identity",
            "host{host.id=H1}[host.name=discarded];host{host.id=H2}",
            Entity.builder("host", Attributes.of(stringKey("host.id"), "H2")).build()));
  }

  @Test
  void conflictingTypesPreserveNonConflictingAttributes() {
    Resource resource = parse("process{process.pid=1}[env=dev];host{host.id=H1}[env=prod]");
    assertThat(EntityUtil.getEntities(resource))
        .containsExactly(
            Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
                .setDescription(Attributes.of(stringKey("env"), "prod"))
                .build());
    assertThat(EntityUtil.getUnassociatedAttributes(resource))
        .isEqualTo(Attributes.of(stringKey("process.pid"), "1"));
    logs.assertContains(
        event -> event.getLevel().equals(WARN),
        "overwrites attributes of previously defined entities");
  }

  @Test
  void repeatedSameTypePreservesLastOccurrenceOrder() {
    Resource resource = parse("host{host.id=H1};service{service.name=S1};host{host.id=H2}");
    assertThat(EntityUtil.getEntities(resource))
        .containsExactlyElementsOf(
            Arrays.asList(
                Entity.builder("service", Attributes.of(stringKey("service.name"), "S1")).build(),
                Entity.builder("host", Attributes.of(stringKey("host.id"), "H2")).build()));
  }
}
