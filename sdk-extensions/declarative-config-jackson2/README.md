# Declarative Configuration Jackson 2 Mapper

This extension provides a Jackson 2 implementation of the `DeclarativeConfigurationMapper` SPI.
It is included as a runtime dependency by `opentelemetry-sdk-extension-declarative-config` from
the `opentelemetry-sdk-extension-declarative-config-jackson2` artifact.

Projects using another mapper can exclude this artifact from the declarative-config dependency and
register their own `DeclarativeConfigurationMapper` provider. For example, a project using Jackson 3
can implement the SPI with its Jackson 3 `ObjectMapper`.
