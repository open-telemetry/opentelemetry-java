#!/bin/bash
set -e

# Add Java 11 compile target and disruptor version update to opencensus-shim build file
cat >> opencensus-shim/build.gradle.kts << 'EOF'

tasks.withType<JavaCompile>().configureEach {
  options.release.set(11)
}

configurations.all {
  resolutionStrategy.force("com.lmax:disruptor:4.0.0")
}
EOF

# Add targeted disruptor fix to exporters/otlp/all for JdkHttpSender tests
if ! grep -q "resolutionStrategy.force.*disruptor.*4.0.0" exporters/otlp/all/build.gradle.kts; then
  # Replace the existing afterEvaluate block with the complete version
  sed -i '/afterEvaluate {/,/^}$/c\
afterEvaluate {\
  tasks.named<JavaCompile>("compileTestJdkHttpSenderJava") {\
    options.release.set(11)\
  }\
\
  // Force disruptor 4.0.0 for JdkHttpSender tests to prevent conflicts with mockserver\
  configurations.named("testJdkHttpSenderRuntimeClasspath") {\
    resolutionStrategy.force("com.lmax:disruptor:4.0.0")\
  }\
}' exporters/otlp/all/build.gradle.kts
fi
