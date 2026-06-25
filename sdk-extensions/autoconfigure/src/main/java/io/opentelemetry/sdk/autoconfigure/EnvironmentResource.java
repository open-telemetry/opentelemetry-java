/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityBuilder;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Creates an OpenTelemetry {@link Resource} from environment configuration.
 *
 * <p>This class is intentionally self-contained (no dependencies on other autoconfigure-internal
 * classes) so that it can be copied wholesale into declarative configuration without pulling in
 * additional dependencies. Do not add dependencies on non-API, non-SPI classes.
 */
final class EnvironmentResource {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  // Visible for testing
  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
  static final String ENTITIES_PROPERTY = "otel.entities";

  /**
   * Create a {@link Resource} from the environment. The resource contains attributes parsed from
   * environment variables and system property keys {@code otel.resource.attributes} and {@code
   * otel.service.name}.
   *
   * @param config the {@link ConfigProperties} used to obtain resource properties
   * @return the resource.
   */
  @SuppressWarnings("JdkObsolete") // Recommended alternative was introduced in java 10
  static Resource createEnvironmentResource(ConfigProperties config) {
    boolean entitiesEnabled = config.getBoolean(EntityExperimentConstants.EXPERIMENTAL_ENTITIES_ENABLED, false);
    if (entitiesEnabled) {
      ResourceBuilder builder = Resource.builder();

      String entitiesStr = config.getString(ENTITIES_PROPERTY);
      if (entitiesStr != null && !entitiesStr.isEmpty()) {
        List<Entity> parsedEntities = new EntityParser(entitiesStr).parse();
        for (Entity entity : parsedEntities) {
          EntityUtil.addEntity(builder, entity);
        }
      }

      String serviceName = config.getString(SERVICE_NAME_PROPERTY);
      if (serviceName != null) {
        Entity serviceEntity =
            Entity.builder("service").setId(Attributes.of(SERVICE_NAME, serviceName)).build();
        EntityUtil.addEntity(builder, serviceEntity);
      }

      for (Map.Entry<String, String> entry : config.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
        builder.put(entry.getKey(), decodeResourceAttributes(entry.getValue()));
      }

      return builder.build();
    }

    AttributesBuilder resourceAttributes = Attributes.builder();
    for (Map.Entry<String, String> entry : config.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
      resourceAttributes.put(
          entry.getKey(),
          // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
          // characters outside the baggage-octet range are percent encoded
          // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
          decodeResourceAttributes(entry.getValue()));
    }
    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName != null) {
      resourceAttributes.put(SERVICE_NAME, serviceName);
    }

    return Resource.create(resourceAttributes.build());
  }

  /**
   * Decodes percent-encoded characters in resource attribute values per W3C Baggage spec.
   *
   * <p>Unlike {@link java.net.URLDecoder}, this method:
   *
   * <ul>
   *   <li>Preserves '+' as a literal plus sign (URLDecoder decodes '+' as space)
   *   <li>Preserves invalid percent sequences as literals (e.g., "%2G", "%", "%2")
   *   <li>Supports multi-byte UTF-8 sequences (e.g., "%C3%A9" decodes to "é")
   * </ul>
   *
   * @param value the percent-encoded string
   * @return the decoded string
   */
  private static String decodeResourceAttributes(String value) {
    // no percent signs means nothing to decode
    if (value.indexOf('%') < 0) {
      return value;
    }

    int n = value.length();
    // Use byte array to properly handle multi-byte UTF-8 sequences
    byte[] bytes = new byte[n];
    int pos = 0;

    for (int i = 0; i < n; i++) {
      char c = value.charAt(i);
      // Check for percent-encoded sequence i.e. '%' followed by two hex digits
      if (c == '%' && i + 2 < n) {
        int d1 = Character.digit(value.charAt(i + 1), 16);
        int d2 = Character.digit(value.charAt(i + 2), 16);
        // Valid hex digits return 0-15, invalid returns -1
        if (d1 != -1 && d2 != -1) {
          // Combine two hex digits into a single byte (e.g., "2F" becomes 0x2F)
          bytes[pos++] = (byte) ((d1 << 4) + d2);
          // Skip the two hex digits (loop will also do i++)
          i += 2;
          continue;
        }
      }
      // Keep '+' as '+' (unlike URLDecoder) and preserve invalid percent sequences which will be
      // treated as literals
      bytes[pos++] = (byte) c;
    }
    return new String(bytes, 0, pos, StandardCharsets.UTF_8);
  }

  private EnvironmentResource() {}

  private static final class Segment {
    private final String source;
    private int start;
    private int end;
    private boolean needsDecoding;

    Segment(String source) {
      this.source = source;
      reset(0);
    }

    void reset(int start) {
      this.start = start;
      this.end = start;
      this.needsDecoding = false;
    }

    void markEnd(int end) {
      this.end = end;
    }

    void markNeedsDecoding() {
      this.needsDecoding = true;
    }

    boolean isEmpty() {
      return start >= end;
    }

    String getValue() {
      if (isEmpty()) {
        return "";
      }
      String substring = source.substring(start, end).trim();
      return needsDecoding ? decodeResourceAttributes(substring) : substring;
    }
  }

  // State machine parser
  private static final class EntityParser {
    private static final Logger logger = Logger.getLogger(EntityParser.class.getName());

    /**
     * The current state of parsing.
     *
     * <p>The format is TYPE{KEY1=VAL1,KEY2=VAL2}[ATTR1=VAL1,ATTR2=VAL2]@SCHEMA_URL;
     *
     * <p>The parser state machine transitions between the following states: - TYPE: Parsing an
     * entity type - ID_KEY: Parsing a "key" of an identity attribute - ID_VAL: Parsing a "value" of
     * an identity attribute - DESC_KEY: Parsing a "key" of a description attribute - DESC_VAL:
     * Parsing a "value" of a description attribute - SCHEMA_URL: Parsing the schema URL of a
     * specific entity - SKIP_TO_NEXT: Skip to the next entity
     */
    private enum State {
      TYPE,
      ID_KEY,
      ID_VAL,
      DESC_KEY,
      DESC_VAL,
      SCHEMA_URL,
      SKIP_TO_NEXT
    }

    /** The input entity string. */
    private final String input;

    /** The current state of parsing. (i.e. where we are in the grammar) */
    private State state = State.TYPE;

    /** The segment of the input string that we are currently parsing. */
    private final Segment currentSegment;

    /** The list of entities we've parsed. */
    private final List<Entity> entities = new ArrayList<>();

    // Temporary state for building an entity.

    /** The parsed entity type. */
    @Nullable private String currentType;

    /** Parsed attributes denoting the entity identity. */
    private Attributes currentIdAttrs = Attributes.empty();

    /** Parsed attributes denoting the entity description. */
    private Attributes currentDescAttrs = Attributes.empty();

    /** Parsed schema URL for the entity. */
    @Nullable private String currentSchemaUrl;

    /** A temporary builder we use when parsing key-value pairs for identity or description. */
    @Nullable private AttributesBuilder currentBuilder;

    /** The current key of a key-value pair that we are parsing. */
    @Nullable private String currentKey;

    EntityParser(String input) {
      this.input = input;
      this.currentSegment = new Segment(input);
    }

    /**
     * Parses the input string and returns a list of entities.
     *
     * @return the list of entities parsed from the input string.
     */
    List<Entity> parse() {
      int n = input.length();
      for (int i = 0; i < n; i++) {
        char c = input.charAt(i);

        // We finished the previous entity, or hit a syntax error.
        // Skip to the next entity and try to parse it.
        if (state == State.SKIP_TO_NEXT) {
          if (c == ';') {
            resetEntityState(i + 1);
            state = State.TYPE;
          }
          continue;
        }

        switch (c) {
          case '{':
            // Finish writing entity type, start identity parsing.
            if (state == State.TYPE) {
              currentSegment.markEnd(i);
              currentType = currentSegment.getValue();
              if (currentType == null || currentType.isEmpty()) {
                logger.log(Level.WARNING, "Malformed entity definition (empty type): " + input);
                state = State.SKIP_TO_NEXT;
              } else {
                state = State.ID_KEY;
                currentSegment.reset(i + 1);
                currentBuilder = Attributes.builder();
              }
            }
            break;
          case '}':
            // End identity parsing.
            if (state == State.ID_VAL || state == State.ID_KEY) {
              currentSegment.markEnd(i);
              if (state == State.ID_VAL) {
                putAttr();
              }
              if (currentBuilder != null) {
                currentIdAttrs = currentBuilder.build();
              }
              if (currentIdAttrs.isEmpty()) {
                logger.log(
                    Level.WARNING,
                    "Malformed entity definition (missing identifying attributes): " + input);
                state = State.SKIP_TO_NEXT;
              } else {
                state = State.TYPE; // Default next state, might change if [ or @ follows
                currentSegment.reset(i + 1);
              }
            }
            break;
          case '[':
            // We finished identity, we're moving to parse description.
            if (state == State.TYPE) {
              // After } we are in TYPE state again but expecting [ or @ or ;
              state = State.DESC_KEY;
              currentSegment.reset(i + 1);
              currentBuilder = Attributes.builder();
            }
            break;
          case ']':
            // We finished description, update attributes for description and move
            // back to TYPE state.
            if (state == State.DESC_VAL || state == State.DESC_KEY) {
              currentSegment.markEnd(i);
              if (state == State.DESC_VAL) {
                putAttr();
              }
              if (currentBuilder != null) {
                currentDescAttrs = currentBuilder.build();
              }
              state = State.TYPE;
              currentSegment.reset(i + 1);
            }
            break;
          case '=':
            // Finish our "key" parsing and start looking for a value.
            if (state == State.ID_KEY || state == State.DESC_KEY) {
              currentSegment.markEnd(i);
              currentKey = currentSegment.getValue();
              if (currentKey == null || currentKey.isEmpty()) {
                logger.log(Level.WARNING, "Malformed key-value pair (empty key): " + input);
                state = State.SKIP_TO_NEXT;
              } else {
                state = (state == State.ID_KEY) ? State.ID_VAL : State.DESC_VAL;
                currentSegment.reset(i + 1);
              }
            }
            break;
          case ',':
            // Finish our "value" parsing and start looking for the next key-value.
            if (state == State.ID_VAL || state == State.DESC_VAL) {
              currentSegment.markEnd(i);
              putAttr();
              state = (state == State.ID_VAL) ? State.ID_KEY : State.DESC_KEY;
              currentSegment.reset(i + 1);
            }
            break;
          case '@':
            // Start looking for schema url
            if (state == State.TYPE) {
              state = State.SCHEMA_URL;
              currentSegment.reset(i + 1);
            }
            break;
          case ';':
            // Finish up the current entity, and get ready to parse the next.
            if (state == State.TYPE || state == State.SCHEMA_URL) {
              if (state == State.SCHEMA_URL) {
                currentSegment.markEnd(i);
                currentSchemaUrl = currentSegment.getValue();
              }
              buildAndAddEntity();
              resetEntityState(i + 1);
              state = State.TYPE;
            } else if (state == State.ID_KEY
                || state == State.ID_VAL
                || state == State.DESC_KEY
                || state == State.DESC_VAL) {
              logger.log(Level.WARNING, "Malformed entity definition (unexpected ';'): " + input);
              resetEntityState(i + 1);
              state = State.TYPE;
            }
            break;
          case '%':
            // Found an escape character, mark the segment as needing decoding, which
            // requires special handling.
            currentSegment.markNeedsDecoding();
            break;
          default:
            break;
        }
      }

      // Handle end of string
      if (state == State.TYPE || state == State.SCHEMA_URL) {
        if (state == State.SCHEMA_URL) {
          currentSegment.markEnd(input.length());
          currentSchemaUrl = currentSegment.getValue();
        }
        buildAndAddEntity();
      }

      return entities;
    }

    /** Adds the current attribute key-value pair into the current attribute builder. */
    private void putAttr() {
      String val = currentSegment.getValue();
      if (currentKey != null && !currentKey.isEmpty() && currentBuilder != null) {
        currentBuilder.put(currentKey, val);
      }
    }

    /** Finishes building the current entity and adds it to the parsed list. */
    private void buildAndAddEntity() {
      if (currentType != null && !currentType.isEmpty() && !currentIdAttrs.isEmpty()) {
        EntityBuilder builder = Entity.builder(currentType).setId(currentIdAttrs);
        if (!currentDescAttrs.isEmpty()) {
          builder.setDescription(currentDescAttrs);
        }
        if (currentSchemaUrl != null && !currentSchemaUrl.isEmpty()) {
          builder.setSchemaUrl(currentSchemaUrl);
        }
        entities.add(builder.build());
      }
    }

    /**
     * Resets the state of the entity parser.
     *
     * @param nextStart the start index of the next entity (e.g. after the `;`).
     */
    private void resetEntityState(int nextStart) {
      currentType = null;
      currentIdAttrs = Attributes.empty();
      currentDescAttrs = Attributes.empty();
      currentSchemaUrl = null;
      currentBuilder = null;
      currentKey = null;
      currentSegment.reset(nextStart);
    }
  }
}
