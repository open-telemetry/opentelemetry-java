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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.constructor.StandardConstructor;
import org.snakeyaml.engine.v2.exceptions.ConstructorException;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
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

  private static final ObjectMapper MAPPER;

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
   * <p>Before parsing, environment variable substitution is performed as described in {@link
   * EnvSubstitutionConstructor}.
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
    Load yaml = new Load(settings, new EnvSubstitutionConstructor(settings, environmentVariables));
    return yaml.loadFromInputStream(inputStream);
  }

  /**
   * Convert the {@code model} to a generic {@link DeclarativeConfigProperties}.
   *
   * @param model the configuration model
   * @return a generic {@link DeclarativeConfigProperties} representation of the model
   */
  public static DeclarativeConfigProperties toConfigProperties(
      OpenTelemetryConfigurationModel model) {
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
    SamplerModel samplerModel = convertToModel(yamlDeclarativeConfigProperties, SamplerModel.class);
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

  static <T> T convertToModel(
      YamlDeclarativeConfigProperties yamlDeclarativeConfigProperties, Class<T> modelType) {
    return MAPPER.convertValue(yamlDeclarativeConfigProperties.toMap(), modelType);
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

  /**
   * {@link StandardConstructor} which substitutes environment variables.
   *
   * <p>Environment variables follow the syntax {@code ${VARIABLE}}, where {@code VARIABLE} is an
   * environment variable matching the regular expression {@code [a-zA-Z_]+[a-zA-Z0-9_]*}.
   *
   * <p>Environment variable substitution only takes place on scalar values of maps. References to
   * environment variables in keys or sets are ignored.
   *
   * <p>If a referenced environment variable is not defined, it is replaced with {@code ""}.
   */
  private static final class EnvSubstitutionConstructor extends StandardConstructor {

    // Load is not thread safe but this instance is always used on the same thread
    private final Load load;
    private final Map<String, String> environmentVariables;

    private EnvSubstitutionConstructor(
        LoadSettings loadSettings, Map<String, String> environmentVariables) {
      super(loadSettings);
      load = new Load(loadSettings);
      this.environmentVariables = environmentVariables;
    }

    /**
     * Implementation is same as {@link
     * org.snakeyaml.engine.v2.constructor.BaseConstructor#constructMapping(MappingNode)} except we
     * override the resolution of values with our custom {@link #constructValueObject(Node)}, which
     * performs environment variable substitution.
     */
    @Override
    @SuppressWarnings({"ReturnValueIgnored", "CatchingUnchecked"})
    protected Map<Object, Object> constructMapping(MappingNode node) {
      Map<Object, Object> mapping = settings.getDefaultMap().apply(node.getValue().size());
      List<NodeTuple> nodeValue = node.getValue();
      for (NodeTuple tuple : nodeValue) {
        Node keyNode = tuple.getKeyNode();
        Object key = constructObject(keyNode);
        if (key != null) {
          try {
            key.hashCode(); // check circular dependencies
          } catch (Exception e) {
            throw new ConstructorException(
                "while constructing a mapping",
                node.getStartMark(),
                "found unacceptable key " + key,
                tuple.getKeyNode().getStartMark(),
                e);
          }
        }
        Node valueNode = tuple.getValueNode();
        Object value = constructValueObject(valueNode);
        if (keyNode.isRecursive()) {
          if (settings.getAllowRecursiveKeys()) {
            postponeMapFilling(mapping, key, value);
          } else {
            throw new YamlEngineException(
                "Recursive key for mapping is detected but it is not configured to be allowed.");
          }
        } else {
          mapping.put(key, value);
        }
      }

      return mapping;
    }

    private static final String ESCAPE_SEQUENCE = "$$";
    private static final int ESCAPE_SEQUENCE_LENGTH = ESCAPE_SEQUENCE.length();
    private static final char ESCAPE_SEQUENCE_REPLACEMENT = '$';

    private Object constructValueObject(Node node) {
      Object value = constructObject(node);
      if (!(node instanceof ScalarNode)) {
        return value;
      }
      if (!(value instanceof String)) {
        return value;
      }

      String val = (String) value;
      ScalarStyle scalarStyle = ((ScalarNode) node).getScalarStyle();

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

      // If the value was double quoted, retain the double quotes so we don't change a value
      // intended to be a string to a different type after environment variable substitution
      if (scalarStyle == ScalarStyle.DOUBLE_QUOTED) {
        newVal.insert(0, "\"");
        newVal.append("\"");
      }
      return load.loadFromString(newVal.toString());
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
