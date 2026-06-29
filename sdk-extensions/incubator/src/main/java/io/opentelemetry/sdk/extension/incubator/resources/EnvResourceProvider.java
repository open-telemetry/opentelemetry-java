/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityBuilder;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * {@link ResourceProvider} and {@link ComponentProvider} for detecting resources via the {@code
 * OTEL_ENTITIES} environment variable.
 */
public final class EnvResourceProvider implements ResourceProvider, ComponentProvider {

  private static final String ENTITIES_PROPERTY = "otel.entities";

  @Override
  public Resource createResource(ConfigProperties config) {
    return createResourceInternal(config);
  }

  @Override
  public int order() {
    return 0;
  }

  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "env";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    ConfigProperties properties =
        DefaultConfigProperties.create(Collections.emptyMap(), config.getComponentLoader());
    return createResourceInternal(properties);
  }

  private static Resource createResourceInternal(ConfigProperties config) {
    String entitiesStr = config.getString(ENTITIES_PROPERTY);
    if (entitiesStr == null || entitiesStr.isEmpty()) {
      return Resource.empty();
    }

    ResourceBuilder builder = Resource.builder();
    List<Entity> parsedEntities = new EntityParser(entitiesStr).parse();
    for (Entity entity : parsedEntities) {
      EntityUtil.addEntity(builder, entity);
    }
    return builder.build();
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
      // Keep '+' as '+' (unlike URLDecoder) and preserve invalid percent sequences
      // which will be
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
