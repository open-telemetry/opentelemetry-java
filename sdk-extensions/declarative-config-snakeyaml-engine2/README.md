# SnakeYAML Engine 2 Declarative Configuration Parser

This module provides the default `DeclarativeConfigurationYamlParser` implementation using
SnakeYAML Engine 2. It is included as a runtime dependency by
`opentelemetry-sdk-extension-declarative-config`.

Applications that provide a custom YAML parser can exclude this module from the declarative config
dependency and register their parser with `ServiceLoader`. If neither this adapter nor another
parser provider is present, YAML parsing fails with a `DeclarativeConfigException`.
