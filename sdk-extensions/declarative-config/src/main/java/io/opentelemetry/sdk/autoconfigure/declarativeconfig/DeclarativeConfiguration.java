/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.DeclarativeConfigProviderLoader;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.DeclarativeConfigurationMapperUtil;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal.SnakeYamlDeclarativeConfigurationParser;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SamplerModel;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configure {@link OpenTelemetrySdk} using <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/configuration#declarative-configuration">declarative
 * configuration</a>. For most users, this means calling {@link #parseAndCreate(InputStream)} with a
 * <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/data-model.md#yaml-file-format">YAML
 * configuration file</a>.
 *
 * <h2>For Implementers</h2>
 *
 * <p>YAML parsing and model mapping can be replaced by implementations of {@link
 * DeclarativeConfigurationYamlParser} and {@link DeclarativeConfigurationMapper}.
 */
public final class DeclarativeConfiguration {

  private static final Logger logger = Logger.getLogger(DeclarativeConfiguration.class.getName());
  private static final ComponentLoader DEFAULT_COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());
  private static final DeclarativeConfigurationYamlParser YAML_PARSER =
      DeclarativeConfigProviderLoader.load(
          DeclarativeConfigurationYamlParser.class, SnakeYamlDeclarativeConfigurationParser::new);

  private DeclarativeConfiguration() {}

  /**
   * Combines {@link #parse(InputStream)} and {@link #create(OpenTelemetryConfigurationModel)}.
   *
   * @throws DeclarativeConfigException if unable to parse or interpret
   */
  public static DeclarativeConfigResult parseAndCreate(InputStream inputStream) {
    OpenTelemetryConfigurationModel configurationModel = parse(inputStream);
    return create(configurationModel);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @return the {@link DeclarativeConfigResult}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static DeclarativeConfigResult create(OpenTelemetryConfigurationModel configurationModel) {
    return create(configurationModel, DEFAULT_COMPONENT_LOADER);
  }

  /**
   * Interpret the {@code configurationModel} to create {@link OpenTelemetrySdk} instance
   * corresponding to the configuration.
   *
   * @param configurationModel the configuration model
   * @param componentLoader the component loader used to load {@link ComponentProvider}
   *     implementations
   * @return the {@link DeclarativeConfigResult}
   * @throws DeclarativeConfigException if unable to interpret
   */
  public static DeclarativeConfigResult create(
      OpenTelemetryConfigurationModel configurationModel, ComponentLoader componentLoader) {
    return create(configurationModel, new DeclarativeConfigContext(componentLoader));
  }

  private static DeclarativeConfigResult create(
      OpenTelemetryConfigurationModel configurationModel, DeclarativeConfigContext context) {
    DeclarativeConfigurationBuilder builder = new DeclarativeConfigurationBuilder();
    context.setBuilder(builder);

    for (DeclarativeConfigurationCustomizerProvider provider :
        Ordered.loadOrderedList(context, DeclarativeConfigurationCustomizerProvider.class)) {
      provider.customize(builder);
    }

    DeclarativeConfigResult result =
        createAndMaybeCleanup(
            OpenTelemetryConfigurationFactory.getInstance(),
            context,
            builder.customizeModel(configurationModel));
    callAutoConfigureListeners(context, result.getSdk());
    return result;
  }

  /**
   * Parse the {@code configuration} YAML and return the {@link OpenTelemetryConfigurationModel}.
   *
   * <p>During parsing, environment variable and system property substitution is performed as
   * defined in the <a
   * href="https://opentelemetry.io/docs/specs/otel/configuration/data-model/#environment-variable-substitution">
   * OpenTelemetry Configuration Data Model specification</a>.
   *
   * @throws DeclarativeConfigException if unable to parse
   */
  public static OpenTelemetryConfigurationModel parse(InputStream configuration) {
    try {
      return parse(configuration, System.getenv(), System.getProperties());
    } catch (RuntimeException e) {
      throw new DeclarativeConfigException("Unable to parse configuration input stream", e);
    }
  }

  // Visible for testing
  static OpenTelemetryConfigurationModel parse(
      InputStream configuration,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties) {
    Object yamlObj = loadYaml(configuration, environmentVariables, systemProperties);
    OpenTelemetryConfigurationModel model =
        DeclarativeConfigurationMapperUtil.convert(yamlObj, OpenTelemetryConfigurationModel.class);
    return Objects.requireNonNull(model, "Unable to map configuration to model");
  }

  // Visible for testing
  static Object loadYaml(
      InputStream inputStream,
      Map<String, String> environmentVariables,
      Map<Object, Object> systemProperties) {
    return YAML_PARSER.parse(inputStream, environmentVariables, systemProperties);
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
    Object yamlObj = loadYaml(configuration, System.getenv(), System.getProperties());
    return toConfigProperties(yamlObj, DEFAULT_COMPONENT_LOADER);
  }

  static DeclarativeConfigProperties toConfigProperties(
      Object model, ComponentLoader componentLoader) {
    Map<String, Object> configurationMap = DeclarativeConfigurationMapperUtil.toMap(model);
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
        Objects.requireNonNull(
            DeclarativeConfigurationMapperUtil.convert(
                DeclarativeConfigProperties.toMap(yamlDeclarativeConfigProperties),
                SamplerModel.class),
            "Unable to map sampler configuration to model");
    return createAndMaybeCleanup(
        SamplerFactory.getInstance(),
        new DeclarativeConfigContext(yamlDeclarativeConfigProperties.getComponentLoader()),
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

  static <M, R> R createAndMaybeCleanup(
      Factory<M, R> factory, DeclarativeConfigContext context, M model) {
    try {
      return factory.create(model, context);
    } catch (RuntimeException e) {
      logger.info("Error encountered interpreting model. Closing partially configured components.");
      for (Closeable closeable : context.getCloseables()) {
        try {
          logger.fine("Closing " + closeable.getClass().getName());
          closeable.close();
        } catch (IOException ex) {
          logger.log(Level.WARNING, "Error closing " + closeable.getClass().getName(), ex);
        }
      }
      if (e instanceof DeclarativeConfigException) {
        throw e;
      }
      throw new DeclarativeConfigException("Unexpected configuration error", e);
    }
  }

  // Visible for testing
  static void callAutoConfigureListeners(
      DeclarativeConfigContext context, OpenTelemetrySdk openTelemetrySdk) {
    for (AutoConfigureListener listener : context.getListeners()) {
      try {
        listener.afterAutoConfigure(openTelemetrySdk);
      } catch (Throwable throwable) {
        logger.log(
            Level.WARNING, "Error invoking listener " + listener.getClass().getName(), throwable);
      }
    }
  }
}
