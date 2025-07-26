/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
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
 * Configure {@link OpenTelemetrySdk} using <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/configuration#declarative-configuration">declarative
 * configuration</a>. For most users, this means calling {@link #parseAndCreate(InputStream)} with a
 * <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/data-model.md#yaml-file-format">YAML
 * configuration file</a>.
 */
public final class DeclarativeConfiguration {

  private static final Logger logger = Logger.getLogger(DeclarativeConfiguration.class.getName());
  private static final Pattern ENV_VARIABLE_REFERENCE =
      Pattern.compile("\\$\\{([a-zA-Z_][a-zA-Z0-9_]*)(:-([^\n}]*))?}");
  private static final ComponentLoader DEFAULT_COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());

  // Visible for testing
  static final ObjectMapper MAPPER;

  static {
    MAPPER =
        new ObjectMapper()
            // Create empty object instances for keys which are present but have null values
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
    // Boxed primitives which are present but have null values should be set to null, rather than
    // empty instances
    MAPPER.configOverride(String.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Integer.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Double.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
    MAPPER.configOverride(Boolean.class).setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.SET));
  }

  private DeclarativeConfiguration() {}

  /**
   * Combines {@link #parse(InputStream)} and {@link #create(OpenTelemetryConfigurationModel)}.
   *
   * @throws DeclarativeConfigException if unable to parse or interpret
   */
  public static OpenTelemetrySdk parseAndCreate(InputStream inputStream) {
    OpenTelemetryConfigurationModel configurationModel = parse(inputStream);
    return create(configurationModel);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @return the {@link OpenTelemetrySdk}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static OpenTelemetrySdk create(OpenTelemetryConfigurationModel configurationModel) {
    return create(configurationModel, DEFAULT_COMPONENT_LOADER);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @param componentLoader the component loader used to load {@link ComponentProvider}
   *     implementations
   * @return the {@link OpenTelemetrySdk}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static OpenTelemetrySdk create(
      OpenTelemetryConfigurationModel configurationModel, ComponentLoader componentLoader) {
    SpiHelper spiHelper = SpiHelper.create(componentLoader);

    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();

    for (DeclarativeConfigurationCustomizerProvider provider :
        spiHelper.loadOrdered(DeclarativeConfigurationCustomizerProvider.class)) {
      provider.customize(builder);
    }

    return createAndMaybeCleanup(
        OpenTelemetryConfigurationFactory.getInstance(),
        spiHelper,
        builder.customizeModel(configurationModel));
  }

  /**
   * Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfigurationModel}.
   *
   * <p>During parsing, environment variable substitution is performed as defined in the <a
   * href="https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution">
   * OpenTelemetry Configuration Data Model specification</a>.
   *
   * @throws DeclarativeConfigException if unable to parse
   */
  public static OpenTelemetryConfigurationModel parse(InputStream configuration) {
    try {
      return parse(configuration, System.getenv());
    } catch (RuntimeException e) {
      throw new DeclarativeConfigException("Unable to parse configuration input stream", e);
    }
  }

  // Visible for testing
  static OpenTelemetryConfigurationModel parse(
      InputStream configuration, Map<String, String> environmentVariables) {
    Object yamlObj = loadYaml(configuration, environmentVariables);
    return MAPPER.convertValue(yamlObj, OpenTelemetryConfigurationModel.class);
  }

  // Visible for testing
  static Object loadYaml(InputStream inputStream, Map<String, String> environmentVariables) {
    LoadSettings settings = LoadSettings.builder().setSchema(new CoreSchema()).build();
    Load yaml = new EnvLoad(settings, environmentVariables);
    return yaml.loadFromInputStream(inputStream);
  }

  /**
   * Convert the {@code model} to a generic {@link DeclarativeConfigProperties}.
   *
   * @param model the configuration model
   * @return a generic {@link DeclarativeConfigProperties} representation of the model
   */
  public static DeclarativeConfigProperties toConfigProperties(Object model) {
    return toConfigProperties(model, DEFAULT_COMPONENT_LOADER);
  }

  /**
   * Convert the {@code configuration} YAML to a generic {@link DeclarativeConfigProperties}.
   *
   * @param configuration configuration YAML
   * @return a generic {@link DeclarativeConfigProperties} representation of the model
   */
  public static DeclarativeConfigProperties toConfigProperties(InputStream configuration) {
    Object yamlObj = loadYaml(configuration, System.getenv());
    return toConfigProperties(yamlObj, DEFAULT_COMPONENT_LOADER);
  }

  static DeclarativeConfigProperties toConfigProperties(
      Object model, ComponentLoader componentLoader) {
    Map<String, Object> configurationMap =
        MAPPER.convertValue(model, new TypeReference<Map<String, Object>>() {});
    if (configurationMap == null) {
      configurationMap = Collections.emptyMap();
    }
    return YamlDeclarativeConfigProperties.create(configurationMap, componentLoader);
  }

  /**
   * Create a {@link SamplerModel} from the {@code samplerModel} representing the sampler config.
   *
   * <p>This is used when samplers are composed, with one sampler accepting one or more additional
   * samplers as config properties. The {@link ComponentProvider} implementation can call this to
   * configure a delegate {@link SamplerModel} from the {@link DeclarativeConfigProperties}
   * corresponding to a particular config property.
   */
  // TODO(jack-berg): add create methods for all SDK extension components supported by
  // ComponentProvider
  public static Sampler createSampler(DeclarativeConfigProperties genericSamplerModel) {
    YamlDeclarativeConfigProperties yamlDeclarativeConfigProperties =
        requireYamlDeclarativeConfigProperties(genericSamplerModel);
    SamplerModel samplerModel =
        MAPPER.convertValue(
            DeclarativeConfigProperties.toMap(yamlDeclarativeConfigProperties), SamplerModel.class);
    return createAndMaybeCleanup(
        SamplerFactory.getInstance(),
        SpiHelper.create(yamlDeclarativeConfigProperties.getComponentLoader()),
        samplerModel);
  }

  private static YamlDeclarativeConfigProperties requireYamlDeclarativeConfigProperties(
      DeclarativeConfigProperties declarativeConfigProperties) {
    if (!(declarativeConfigProperties instanceof YamlDeclarativeConfigProperties)) {
      throw new DeclarativeConfigException(
          "Only YamlDeclarativeConfigProperties can be converted to model");
    }
    return (YamlDeclarativeConfigProperties) declarativeConfigProperties;
  }

  static <M, R> R createAndMaybeCleanup(Factory<M, R> factory, SpiHelper spiHelper, M model) {
    DeclarativeConfigContext context = new DeclarativeConfigContext(spiHelper);
    try {
      return factory.create(model, context);
    } catch (RuntimeException e) {
      logger.info("Error encountered interpreting model. Closing partially configured components.");
      for (Closeable closeable : context.getCloseables()) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.warning(
              "Error closing " + closeable.getClass().getName() + ": " + ex.getMessage());
        }
      }
      if (e instanceof DeclarativeConfigException) {
        throw e;
      }
      throw new DeclarativeConfigException("Unexpected configuration error", e);
    }
  }

  private static final class EnvLoad extends Load {

    private final LoadSettings settings;
    private final Map<String, String> environmentVariables;

    private EnvLoad(LoadSettings settings, Map<String, String> environmentVariables) {
      super(settings);
      this.settings = settings;
      this.environmentVariables = environmentVariables;
    }

    @Override
    public Object loadFromInputStream(InputStream yamlStream) {
      Objects.requireNonNull(yamlStream, "InputStream cannot be null");
      return loadOne(
          new EnvComposer(
              settings,
              new ParserImpl(
                  settings, new StreamReader(settings, new YamlUnicodeReader(yamlStream))),
              environmentVariables));
    }
  }

  /**
   * A YAML Composer that performs environment variable substitution according to the <a
   * href="https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution">
   * OpenTelemetry Configuration Data Model specification</a>.
   *
   * <p>This composer supports:
   *
   * <ul>
   *   <li>Environment variable references: {@code ${ENV_VAR}} or {@code ${env:ENV_VAR}}
   *   <li>Default values: {@code ${ENV_VAR:-default_value}}
   *   <li>Escape sequences: {@code $$} is replaced with a single {@code $}
   * </ul>
   *
   * <p>Environment variable substitution only applies to scalar values. Mapping keys are not
   * candidates for substitution. Referenced environment variables that are undefined, null, or
   * empty are replaced with empty values unless a default value is provided.
   *
   * <p>The {@code $} character serves as an escape sequence where {@code $$} in the input is
   * translated to a single {@code $} in the output. This prevents environment variable substitution
   * for the escaped content.
   */
  private static final class EnvComposer extends Composer {

    private final Load load;
    private final Map<String, String> environmentVariables;
    private final ScalarResolver scalarResolver;

    private static final String ESCAPE_SEQUENCE = "$$";
    private static final int ESCAPE_SEQUENCE_LENGTH = ESCAPE_SEQUENCE.length();
    private static final char ESCAPE_SEQUENCE_REPLACEMENT = '$';

    private EnvComposer(
        LoadSettings settings, ParserImpl parser, Map<String, String> environmentVariables) {
      super(settings, parser);
      this.load = new Load(settings);
      this.environmentVariables = environmentVariables;
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
      // Iterate through val left to right, search for escape sequence "$$"
      // For the substring of val between the last escape sequence and the next found, perform
      // environment variable substitution
      // Add the escape replacement character '$' in place of each escape sequence found

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
        String envVarKey = matcher.group(1);
        String defaultValue = matcher.group(3);
        if (defaultValue == null) {
          defaultValue = "";
        }
        String replacement = environmentVariables.getOrDefault(envVarKey, defaultValue);
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
