# Testing Patterns

## Assertion library

Use [AssertJ](https://assertj.github.io/doc/) for all assertions.

```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(result).isEqualTo(expected);
assertThat(list).hasSize(3).contains("foo");
assertThatThrownBy(() -> doThing()).isInstanceOf(IllegalArgumentException.class);
```

For SDK signal data (spans, metrics, logs), use the OTel-specific assertj extensions from
`sdk:testing`:

```java
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
```

These provide fluent assertions on `SpanData`, `MetricData`, `LogRecordData`, etc.

## Clocks and time

Avoid `System.currentTimeMillis()` or `Instant.now()` in tests. Use `TestClock` from
`sdk:testing` for deterministic time control:

```java
TestClock clock = TestClock.create();
clock.advance(Duration.ofSeconds(1));
```

## Equals and hashCode

Use [EqualsVerifier](https://jqno.nl/equalsverifier/) to verify `equals`/`hashCode` contracts:

```java
EqualsVerifier.forClass(MyValueClass.class).verify();
```

Available in all test suites via `otel.java-conventions`.

## Log capture

Use [LogUnit](https://github.com/netmikey/logunit) to assert on log output:

```java
@RegisterExtension
LogCapturer logs = LogCapturer.create().captureForType(MyClass.class);

@Test
void logsWarning() {
  // trigger code under test
  logs.assertContains("expected warning text");
}
```

`logunit-jul` is included in all test suites via `otel.java-conventions`.

## Mocking

Mockito is available in all test suites via `otel.java-conventions`. Use
`@ExtendWith(MockitoExtension.class)` with `@Mock` fields, or `Mockito.mock(...)` inline.

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyTest {
  @Mock MetricReader reader;
}
```

The byte-buddy agent is pre-attached by `otel.java-conventions` to suppress dynamic-agent
warnings on Java 21+. No extra configuration needed.

## Shared test utilities

Do **not** use the `java-test-fixtures` plugin. It adds test dependencies to published POMs and
creates a separate `*-test-fixtures.jar` artifact. Shared test utilities belong in a
`*:testing-internal` module.

The root `:testing-internal` module (`/testing-internal/`) contains utilities available across
all modules. It is included automatically by `otel.java-conventions` as a test dependency.

For module-group-specific utilities, create a sibling `testing-internal` module:

```
sdk/
  metrics/
    src/test/java/...        ← module-specific tests
  testing/                   ← published SDK testing utilities (sdk:testing)
    src/main/java/...
```

`sdk:testing` is published and intended for use by external consumers testing OTel integrations.
Keep it stable — treat it as public API.

## Test framework

JUnit 5 (Jupiter). `otel.java-conventions` configures `useJUnitJupiter()` for all test suites.

- Use `@Test`, `@BeforeEach`, `@AfterEach`, `@ParameterizedTest` from `org.junit.jupiter.api`.
- Use `@RegisterExtension` for JUnit 5 extensions (not `@Rule`).
- `@ExtendWith(MockitoExtension.class)` for Mockito injection.

## Test suites

This repo uses Gradle's `JvmTestSuite` API (not manual `Test` task registration).

```kotlin
testing {
  suites {
    register<JvmTestSuite>("testIncubating") {
      dependencies {
        implementation(project(":api:incubator"))
      }
    }
  }
}

tasks {
  check {
    dependsOn(testing.suites) // wires all suites into check
  }
}
```

Rules:
- All registered test suites must be wired into `check` — omitting this causes them to be
  silently skipped in CI.
- Each suite has its own source set under `src/<suiteName>/java/`.
- Suite-specific JVM args go in a `targets { all { testTask.configure { ... } } }` block.
