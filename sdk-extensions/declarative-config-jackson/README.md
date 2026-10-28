# Declarative Configuration Jackson Mapper

This optional extension provides a Jackson 2 implementation of the
`DeclarativeConfigurationMapper` SPI for `opentelemetry-sdk-extension-declarative-config`.

Add this artifact when using the default Jackson 2 mapper. Projects using another mapper can
exclude this artifact and register their own `DeclarativeConfigurationMapper` provider. For
example, a project using Jackson 3 can implement the SPI with its Jackson 3 `ObjectMapper`.
