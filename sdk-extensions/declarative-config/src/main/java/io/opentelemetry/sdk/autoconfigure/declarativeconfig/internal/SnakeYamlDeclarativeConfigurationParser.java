/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.DeclarativeConfigurationYamlParser;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.YamlUnicodeReader;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.snakeyaml.engine.v2.schema.CoreSchema;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class SnakeYamlDeclarativeConfigurationParser
    implements DeclarativeConfigurationYamlParser {

  // Matches ${VAR_NAME}, ${env:VAR_NAME}, or ${sys:property.name} with optional :-default
  private static final Pattern ENV_VARIABLE_REFERENCE =
      Pattern.compile("\\$\\{(?:(env|sys):)?([a-zA-Z_][a-zA-Z0-9_.]*)(?::-([^\\n}]*))?}");

  private static final String ESCAPE_SEQUENCE = "$$";
  private static final int ESCAPE_SEQUENCE_LENGTH = ESCAPE_SEQUENCE.length();
  private static final char ESCAPE_SEQUENCE_REPLACEMENT = '$';

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }

  @Override
  public Object parse(
      InputStream configuration,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties) {
    LoadSettings settings = LoadSettings.builder().setSchema(new CoreSchema()).build();
    Load yaml = new EnvLoad(settings, environmentVariables, systemProperties);
    return yaml.loadFromInputStream(configuration);
  }

  private static final class EnvLoad extends Load {

    private final LoadSettings settings;
    private final Map<String, String> environmentVariables;
    private final Map<Object, Object> systemProperties;

    private EnvLoad(
        LoadSettings settings,
        Map<String, String> environmentVariables,
        Map<Object, Object> systemProperties) {
      super(settings);
      this.settings = settings;
      this.environmentVariables = environmentVariables;
      this.systemProperties = systemProperties;
    }

    @Override
    public Object loadFromInputStream(InputStream yamlStream) {
      Objects.requireNonNull(yamlStream, "InputStream cannot be null");
      return loadOne(
          new EnvComposer(
              settings,
              new ParserImpl(
                  settings, new StreamReader(settings, new YamlUnicodeReader(yamlStream))),
              environmentVariables,
              systemProperties));
    }
  }

  /**
   * A YAML Composer that performs environment variable and system property substitution according
   * to the OpenTelemetry Configuration Data Model specification.
   */
  private static final class EnvComposer extends Composer {

    private final Load load;
    private final Map<String, String> environmentVariables;
    private final Map<Object, Object> systemProperties;
    private final ScalarResolver scalarResolver;

    private EnvComposer(
        LoadSettings settings,
        ParserImpl parser,
        Map<String, String> environmentVariables,
        Map<Object, Object> systemProperties) {
      super(settings, parser);
      this.load = new Load(settings);
      this.environmentVariables = environmentVariables;
      this.systemProperties = systemProperties;
      this.scalarResolver = settings.getSchema().getScalarResolver();
    }

    @Override
    protected Node composeValueNode(MappingNode node) {
      Node itemValue = super.composeValueNode(node);
      if (!(itemValue instanceof ScalarNode)) {
        // Only apply environment variable substitution to ScalarNodes
        return itemValue;
      }
      ScalarNode scalarNode = (ScalarNode) itemValue;
      String envSubstitution = envSubstitution(scalarNode.getValue());

      // If the environment variable substitution does not change the value, do not modify the node
      if (envSubstitution.equals(scalarNode.getValue())) {
        return itemValue;
      }

      Object envSubstitutionObj = load.loadFromString(envSubstitution);
      Tag tag = itemValue.getTag();
      ScalarStyle scalarStyle = scalarNode.getScalarStyle();

      Tag resolvedTag =
          envSubstitutionObj == null
              ? Tag.NULL
              : scalarResolver.resolve(envSubstitutionObj.toString(), true);

      // Only non-quoted substituted scalars can have their tag changed
      if (!itemValue.getTag().equals(resolvedTag)
          && scalarStyle != ScalarStyle.SINGLE_QUOTED
          && scalarStyle != ScalarStyle.DOUBLE_QUOTED) {
        tag = resolvedTag;
      }

      boolean resolved = true;
      return new ScalarNode(
          tag,
          resolved,
          envSubstitution,
          scalarStyle,
          itemValue.getStartMark(),
          itemValue.getEndMark());
    }

    private String envSubstitution(String val) {
      // Substitute each portion between escaped dollar signs independently. Each "$$" becomes
      // one literal "$" and prevents substitution in the adjacent portion.
      int lastEscapeIndexEnd = 0;
      StringBuilder newVal = null;
      while (true) {
        int escapeIndex = val.indexOf(ESCAPE_SEQUENCE, lastEscapeIndexEnd);
        int substitutionEndIndex = escapeIndex == -1 ? val.length() : escapeIndex;
        newVal = envVarSubstitution(newVal, val, lastEscapeIndexEnd, substitutionEndIndex);
        if (escapeIndex == -1) {
          break;
        } else {
          newVal.append(ESCAPE_SEQUENCE_REPLACEMENT);
        }
        lastEscapeIndexEnd = escapeIndex + ESCAPE_SEQUENCE_LENGTH;
        if (lastEscapeIndexEnd >= val.length()) {
          break;
        }
      }

      return newVal.toString();
    }

    private StringBuilder envVarSubstitution(
        @Nullable StringBuilder newVal, String source, int startIndex, int endIndex) {
      String val = source.substring(startIndex, endIndex);
      Matcher matcher = ENV_VARIABLE_REFERENCE.matcher(val);

      if (!matcher.find()) {
        return newVal == null ? new StringBuilder(val) : newVal.append(val);
      }

      if (newVal == null) {
        newVal = new StringBuilder();
      }

      int offset = 0;
      do {
        MatchResult matchResult = matcher.toMatchResult();
        String prefix = matcher.group(1); // "env", "sys", or null
        String key = matcher.group(2); // variable/property name
        String defaultValue = matcher.group(3);
        if (defaultValue == null) {
          defaultValue = "";
        }

        String replacement;
        if ("sys".equals(prefix)) {
          // System property substitution
          Object sysProp = systemProperties.get(key);
          replacement = sysProp != null ? sysProp.toString() : defaultValue;
        } else {
          // Environment variable substitution (default or explicit "env:" prefix)
          replacement = environmentVariables.getOrDefault(key, defaultValue);
        }

        newVal.append(val, offset, matchResult.start()).append(replacement);
        offset = matchResult.end();
      } while (matcher.find());
      if (offset != val.length()) {
        newVal.append(val, offset, val.length());
      }

      return newVal;
    }
  }
}
