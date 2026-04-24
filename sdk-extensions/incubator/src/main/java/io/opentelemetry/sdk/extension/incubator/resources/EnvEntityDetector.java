/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/*
 * An EntityDetector that parses the OTEL_ENTITIES environment variable.
 *
 * See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/entities/entity-propagation.md
 * for more information about the OTEL_ENTITIES environment variable.
 */
class EnvEntityDetector implements EntityDetector {

  private static final Logger logger = Logger.getLogger(EnvEntityDetector.class.getName());
  private static final String PROPERTY_KEY = "otel.entities";

  @Override
  public Collection<Entity> detect(ConfigProperties config) {
    String entitiesStr = config.getString(PROPERTY_KEY);
    if (entitiesStr == null || entitiesStr.isEmpty()) {
      return new ArrayList<>();
    }

    return new EntityParser(entitiesStr).parse();
  }

  /**
   * Segment class represents a start/stop endpoint within a source String.
   *
   * <p>
   * A segment can be used to extract a substring from the source string *without
   * interning* the
   * string into the JDK's string tables. This can dramatically reduce allocations
   * when parsing.
   * Segment is intended to provide a similar interface to using {@code substring}
   * on {@code
   * String}.
   *
   * <p>
   * Additionally, a Segment can be denoted as URL-encoded (e.g. using '%20' to
   * denote a
   * character.) In this case, the segment will be decoded when extracting its
   * String value.
   */
  private static final class Segment {
    private final String source;
    private int start;
    private int end;
    private boolean needsDecoding;

    Segment(String source) {
      this.source = source;
      reset(0);
    }

    /** Reset the segment for the next use, starting from the given start index. */
    void reset(int start) {
      this.start = start;
      this.end = start;
      this.needsDecoding = false;
    }

    /** Update the end of the segment (non-inclusive). */
    void markEnd(int end) {
      this.end = end;
    }

    /**
     * Denotes that the segment is URL encoded, and should be decoded when calling
     * {@code
     * getValue()}.
     */
    void markNeedsDecoding() {
      this.needsDecoding = true;
    }

    /** Return true if the segment is empty. */
    boolean isEmpty() {
      return start >= end;
    }

    /**
     * Returns the string represented by the bounds of the segment *and* decodes it
     * if {@code
     * markNeedsDecoding} has been called.
     *
     * <p>
     * Note: This will trim whitespace from the segment before returning it.
     */
    String getValue() {
      if (isEmpty()) {
        return "";
      }
      // TODO - avoid using substring and then triming to avoid interning more than
      // one string.
      String substring = source.substring(start, end).trim();
      return needsDecoding ? decode(substring) : substring;
    }

    // Percent decoding logic moved here
    private static String decode(String value) {
      if (value.indexOf('%') < 0) {
        return value;
      }

      int n = value.length();
      byte[] bytes = new byte[n];
      int pos = 0;

      for (int i = 0; i < n; i++) {
        char c = value.charAt(i);
        if (c == '%' && i + 2 < n) {
          int d1 = Character.digit(value.charAt(i + 1), 16);
          int d2 = Character.digit(value.charAt(i + 2), 16);
          if (d1 != -1 && d2 != -1) {
            bytes[pos++] = (byte) ((d1 << 4) + d2);
            i += 2;
            continue;
          }
        }
        bytes[pos++] = (byte) c;
      }
      return new String(bytes, 0, pos, StandardCharsets.UTF_8);
    }
  }

  // State machine parser
  private static final class EntityParser {
    /**
     * The current state of parsing.
     *
     * <p>
     * The format is TYPE{KEY1=VAL1,KEY2=VAL2}[ATTR1=VAL1,ATTR2=VAL2]@SCHEMA_URL;
     *
     * <p>
     * The parser state machine transitions between the following states: - TYPE:
     * Parsing an
     * entity type - ID_KEY: Parsing a "key" of an identity attribute - ID_VAL:
     * Parsing a "value" of
     * an identity attribute - DESC_KEY: Parsing a "key" of a description attribute
     * - DESC_VAL:
     * Parsing a "value" of a description attribute - SCHEMA_URL: Parsing the schema
     * URL of a
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
      // TODO - do we need specific states to represent "TYPE_COMPLETE",
      // "ID_COMPLETE", "DESC_COMPLETE"?
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
    @Nullable
    private String currentType;

    /** Parsed attributes denoting the entity identity. */
    private Attributes currentIdAttrs = Attributes.empty();

    /** Parsed attributes denoting the entity description. */
    private Attributes currentDescAttrs = Attributes.empty();

    /** Parsed schema URL for the entity. */
    @Nullable
    private String currentSchemaUrl;

    /**
     * A temporary builder we use when parsing key-value pairs for identity or
     * description.
     */
    @Nullable
    private AttributesBuilder currentBuilder;

    /** The current key of a key-value pair that we are parsing. */
    @Nullable
    private String currentKey;

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
              // TODO - Should we create new state to denote "ID_COMPLETE" for this?
              state = State.DESC_KEY;
              currentSegment.reset(i + 1);
              currentBuilder = Attributes.builder();
            }
            break;
          case ']':
            // We finished description, update attributes for description and move
            // back to TYPE state.
            // TODO - should we create a new state to denote "DESC_COMPLETE"?
            // Since DESC is optional, we would would transition to the same state as
            // ID_COMPLETE, but
            // not allowing DESC to show up again.
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
            if (state == State.TYPE) { // After } or ] we are in TYPE state
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
            // Keep scanning
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

    /**
     * Adds the current attribute key-value pair into the current attribute builder.
     */
    private void putAttr() {
      String val = currentSegment.getValue();
      if (currentKey != null && !currentKey.isEmpty() && currentBuilder != null) {
        currentBuilder.put(currentKey, val);
      }
    }

    /** Finishes building the current entity and adds it to the parsed list. */
    private void buildAndAddEntity() {
      if (currentType != null && !currentType.isEmpty() && !currentIdAttrs.isEmpty()) {
        EntityBuilder builder = Entity.builder(currentType).setIdentity(currentIdAttrs);
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
