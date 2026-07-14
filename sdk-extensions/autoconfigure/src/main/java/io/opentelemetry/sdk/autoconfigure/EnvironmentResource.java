/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
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

  // Visible for testing
  static final String RESOURCE_ATTRIBUTES_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
  static final String ENTITIES_PROPERTY = "otel.entities";
  static final String EXPERIMENTAL_ENTITIES_ENABLED = "otel.experimental.entities.enabled";

  /**
   * Create a {@link Resource} from the environment. The resource contains attributes parsed from
   * environment variables and system property keys {@code otel.resource.attributes} and {@code
   * otel.service.name}.
   *
   * @param config the {@link ConfigProperties} used to obtain resource properties
   * @return the resource.
   */
  @SuppressWarnings("JdkObsolete") // Recommended alternative was introduced in java 10
  static Resource otelResourceAttributesResource(ConfigProperties config) {
    ResourceBuilder resourceBuilder = Resource.builder();

    for (Map.Entry<String, String> entry : config.getMap(RESOURCE_ATTRIBUTES_PROPERTY).entrySet()) {
      resourceBuilder.put(
          entry.getKey(),
          // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
          // characters outside the baggage-octet range are percent encoded
          // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
          decodeResourceAttributes(entry.getValue()));
    }

    return resourceBuilder.build();
  }

  static Resource otelEntitiesResource(ConfigProperties config) {
    ResourceBuilder resourceBuilder = Resource.builder();

    boolean entitiesEnabled = config.getBoolean(EXPERIMENTAL_ENTITIES_ENABLED, false);

    String entitiesStr = config.getString(ENTITIES_PROPERTY);
    if (entitiesEnabled && entitiesStr != null && !entitiesStr.isEmpty()) {
      List<Entity> parsedEntities = new EntityParser(entitiesStr).parse();
      for (Entity entity : parsedEntities) {
        EntityUtil.addEntity(resourceBuilder, entity);
      }
    }

    return resourceBuilder.build();
  }

  static Resource otelServiceNameResource(ConfigProperties config) {
    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName == null) {
      return Resource.empty();
    }

    Entity serviceEntity =
        Entity.builder(
                SemConvAttributes.SERVICE_TYPE,
                Attributes.of(SemConvAttributes.SERVICE_NAME, serviceName))
            .setSchemaUrl(SemConvAttributes.SCHEMA_URL_V1_40_0)
            .build();
    return EntityUtil.addEntity(Resource.builder(), serviceEntity).build();
  }

  static Resource eraseEntities(Resource resource) {
    ResourceBuilder entityErasedBuilder = Resource.builder().putAll(resource.getAttributes());
    // TODO: entities include schemaUrl, which the resource merge algorithm retains as long as all
    // entities agree. However, schemaUrl is not set by by pre-entity resource detectors. Preserving
    // the schemaUrl of while erasing entities adds new schemaUrl to the resource when none was
    // typically used before. Therefore, we do not map the schemaUrl.
    //    if (resource.getSchemaUrl() != null) {
    //      entityErasedBuilder.setSchemaUrl(resource.getSchemaUrl());
    //    }
    return entityErasedBuilder.build();
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

    private enum State {
      TYPE,
      ID_KEY,
      ID_VAL,
      DESC_KEY,
      DESC_VAL,
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
    @Nullable private AttributesBuilder currentBuilder;
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

        switch (c) {
          case '{':
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
                state = State.TYPE;
                currentSegment.reset(i + 1);
              }
            }
            break;
          case '[':
            if (state == State.TYPE) {
              state = State.DESC_KEY;
              currentSegment.reset(i + 1);
              currentBuilder = Attributes.builder();
            }
            break;
          case ']':
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
            if (state == State.ID_VAL || state == State.DESC_VAL) {
              currentSegment.markEnd(i);
              putAttr();
              state = (state == State.ID_VAL) ? State.ID_KEY : State.DESC_KEY;
              currentSegment.reset(i + 1);
            }
            break;
          case '@':
            if (state == State.TYPE) {
              state = State.SCHEMA_URL;
              currentSegment.reset(i + 1);
            }
            break;
          case ';':
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
            currentSegment.markNeedsDecoding();
            break;
          default:
            break;
        }
      }

      if (state == State.TYPE || state == State.SCHEMA_URL) {
        if (state == State.SCHEMA_URL) {
          currentSegment.markEnd(input.length());
          currentSchemaUrl = currentSegment.getValue();
        }
        buildAndAddEntity();
      }

      return entities;
    }

    private void putAttr() {
      String val = currentSegment.getValue();
      if (currentKey != null && !currentKey.isEmpty() && currentBuilder != null) {
        currentBuilder.put(currentKey, val);
      }
    }

    private void buildAndAddEntity() {
      if (currentType != null && !currentType.isEmpty() && !currentIdAttrs.isEmpty()) {
        EntityBuilder builder = Entity.builder(currentType, currentIdAttrs);
        if (!currentDescAttrs.isEmpty()) {
          builder.setDescription(currentDescAttrs);
        }
        if (currentSchemaUrl != null && !currentSchemaUrl.isEmpty()) {
          builder.setSchemaUrl(currentSchemaUrl);
        }
        entities.add(builder.build());
      }
    }

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

  private EnvironmentResource() {}
}
