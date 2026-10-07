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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    ResourceBuilder resourceBuilder =
        addEntities(Resource.builder(), config.getString(ENTITIES_PROPERTY));
    for (Map.Entry<String, String> entry : config.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
      resourceBuilder.put(
          entry.getKey(),
          // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
          // characters outside the baggage-octet range are percent encoded
          // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
          decodeResourceAttributes(entry.getValue()));
    }
    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName != null) {
      resourceBuilder.put(SERVICE_NAME, serviceName);
    }

    return resourceBuilder.build();
  }

  /**
   * Create a {@link Resource} containing only the entities parsed from {@code otel.entities}.
   *
   * @param entities the {@code otel.entities} value, or null if unset
   * @return the resource.
   */
  static Resource createEntitiesResource(@Nullable String entities) {
    return addEntities(Resource.builder(), entities).build();
  }

  private static ResourceBuilder addEntities(
      ResourceBuilder resourceBuilder, @Nullable String entities) {
    if (entities != null && !entities.isEmpty()) {
      for (Entity entity : new EntityParser(entities).parse()) {
        EntityUtil.addEntity(resourceBuilder, entity);
      }
    }
    return resourceBuilder;
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
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9._-]*");

    private enum State {
      TYPE,
      ID_KEY,
      ID_VAL,
      AFTER_ID,
      DESC_KEY,
      DESC_VAL,
      AFTER_DESC,
      SCHEMA_URL,
      SKIP_TO_NEXT
    }

    private final String input;
    private State state = State.TYPE;
    private final Segment currentSegment;
    private final List<Entity> entities = new ArrayList<>();

    @Nullable private String currentType;
    private Attributes currentIdAttrs = Attributes.empty();
    private Attributes currentDescAttrs = Attributes.empty();
    @Nullable private String currentSchemaUrl;
    private AttributesBuilder currentBuilder = Attributes.builder();
    @Nullable private String currentKey;

    EntityParser(String input) {
      this.input = input;
      this.currentSegment = new Segment(input);
    }

    List<Entity> parse() {
      int n = input.length();
      for (int i = 0; i < n; i++) {
        char c = input.charAt(i);

        if (state == State.SKIP_TO_NEXT) {
          if (c == ';') {
            resetEntityState(i + 1);
            state = State.TYPE;
          }
          continue;
        }
        if (state == State.SCHEMA_URL && c != ';') {
          continue;
        }

        switch (c) {
          case '{':
            if (state == State.TYPE) {
              currentSegment.markEnd(i);
              currentType = currentSegment.getValue();
              if (!NAME_PATTERN.matcher(currentType).matches()) {
                malformed("invalid type");
              } else {
                state = State.ID_KEY;
                currentSegment.reset(i + 1);
                currentBuilder = Attributes.builder();
              }
            } else {
              malformed("unexpected '{'");
            }
            break;
          case '}':
            if (state == State.ID_VAL || state == State.ID_KEY) {
              currentSegment.markEnd(i);
              if (state == State.ID_KEY
                  && (!currentSegment.getValue().isEmpty() || currentKey != null)) {
                malformed("missing identifying attribute value");
                break;
              }
              if (state == State.ID_VAL) {
                putAttr();
              }
              currentIdAttrs = currentBuilder.build();
              if (currentIdAttrs.isEmpty()) {
                malformed("missing identifying attributes");
              } else {
                state = State.AFTER_ID;
                currentSegment.reset(i + 1);
              }
            } else {
              malformed("unexpected '}'");
            }
            break;
          case '[':
            if (state == State.AFTER_ID) {
              state = State.DESC_KEY;
              currentSegment.reset(i + 1);
              currentBuilder = Attributes.builder();
              currentKey = null;
            } else {
              malformed("unexpected '['");
            }
            break;
          case ']':
            if (state == State.DESC_VAL || state == State.DESC_KEY) {
              currentSegment.markEnd(i);
              if (state == State.DESC_KEY
                  && (!currentSegment.getValue().isEmpty() || currentKey != null)) {
                malformed("missing descriptive attribute value");
                break;
              }
              if (state == State.DESC_VAL) {
                putAttr();
              }
              currentDescAttrs = currentBuilder.build();
              state = State.AFTER_DESC;
              currentSegment.reset(i + 1);
            } else {
              malformed("unexpected ']'");
            }
            break;
          case '=':
            if (state == State.ID_KEY || state == State.DESC_KEY) {
              currentSegment.markEnd(i);
              currentKey = currentSegment.getValue();
              if (!NAME_PATTERN.matcher(currentKey).matches()) {
                malformed("invalid attribute key");
              } else {
                state = (state == State.ID_KEY) ? State.ID_VAL : State.DESC_VAL;
                currentSegment.reset(i + 1);
              }
            } else {
              malformed("unexpected '='");
            }
            break;
          case ',':
            if (state == State.ID_VAL || state == State.DESC_VAL) {
              currentSegment.markEnd(i);
              putAttr();
              state = (state == State.ID_VAL) ? State.ID_KEY : State.DESC_KEY;
              currentSegment.reset(i + 1);
            } else {
              malformed("unexpected ','");
            }
            break;
          case '@':
            if (state == State.AFTER_ID || state == State.AFTER_DESC) {
              state = State.SCHEMA_URL;
              currentSegment.reset(i + 1);
            } else {
              malformed("unexpected '@'");
            }
            break;
          case ';':
            if (state == State.AFTER_ID || state == State.AFTER_DESC || state == State.SCHEMA_URL) {
              if (state == State.SCHEMA_URL) {
                currentSegment.markEnd(i);
                currentSchemaUrl = currentSegment.getValue();
              }
              buildAndAddEntity();
            } else {
              currentSegment.markEnd(i);
              if (state != State.TYPE || !currentSegment.getValue().isEmpty()) {
                malformed("unexpected ';'");
              }
            }
            resetEntityState(i + 1);
            state = State.TYPE;
            break;
          case '%':
            if (state == State.ID_VAL || state == State.DESC_VAL) {
              currentSegment.markNeedsDecoding();
            } else {
              malformed("unexpected '%'");
            }
            break;
          default:
            if ((state == State.AFTER_ID || state == State.AFTER_DESC)
                && !Character.isWhitespace(c)) {
              malformed("unexpected trailing characters");
            }
            break;
        }
      }

      if (state == State.AFTER_ID || state == State.AFTER_DESC || state == State.SCHEMA_URL) {
        if (state == State.SCHEMA_URL) {
          currentSegment.markEnd(input.length());
          currentSchemaUrl = currentSegment.getValue();
        }
        buildAndAddEntity();
      } else if (state != State.SKIP_TO_NEXT) {
        currentSegment.markEnd(input.length());
        if (state != State.TYPE || !currentSegment.getValue().isEmpty()) {
          malformed("incomplete definition");
        }
      }

      return entities;
    }

    private void putAttr() {
      if (currentKey != null) {
        currentBuilder.put(currentKey, currentSegment.getValue());
      }
    }

    // Only reached after a valid type and nonempty identifying attributes were parsed.
    private void buildAndAddEntity() {
      if (currentType == null) {
        return;
      }
      try {
        EntityBuilder builder =
            Entity.builder(currentType, currentIdAttrs).setDescription(currentDescAttrs);
        String schemaUrl = validateSchemaUrl(currentSchemaUrl);
        if (schemaUrl != null) {
          builder.setSchemaUrl(schemaUrl);
        }
        Entity entity = builder.build();
        if (entities.removeIf(previous -> previous.getType().equals(entity.getType()))) {
          logger.warning(
              "Duplicate entity type [" + entity.getType() + "]; using the last definition.");
        }
        entities.add(entity);
      } catch (IllegalArgumentException e) {
        logger.log(Level.WARNING, "Malformed entity definition: " + input, e);
      }
    }

    private void malformed(String reason) {
      logger.warning("Malformed entity definition (" + reason + "): " + input);
      state = State.SKIP_TO_NEXT;
    }

    @Nullable
    private static String validateSchemaUrl(@Nullable String schemaUrl) {
      if (schemaUrl == null) {
        return null;
      }
      try {
        if (!new URI(schemaUrl).isAbsolute()) {
          logger.warning("Ignoring invalid entity schema URL: " + schemaUrl);
          return null;
        }
        return schemaUrl;
      } catch (URISyntaxException e) {
        logger.log(Level.WARNING, "Ignoring invalid entity schema URL: " + schemaUrl, e);
        return null;
      }
    }

    private void resetEntityState(int nextStart) {
      currentType = null;
      currentIdAttrs = Attributes.empty();
      currentDescAttrs = Attributes.empty();
      currentSchemaUrl = null;
      currentBuilder = Attributes.builder();
      currentKey = null;
      currentSegment.reset(nextStart);
    }
  }

  private EnvironmentResource() {}
}
