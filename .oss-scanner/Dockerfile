FROM eclipse-temurin:25-jdk-noble

ENV DISABLE_REMOTE_BUILD_CACHE=1

COPY . /src
WORKDIR /src

RUN ./gradlew --no-daemon --max-workers=2 \
    -Porg.gradle.java.installations.auto-download=false build

ADD https://raw.githubusercontent.com/open-telemetry/.github/main/SECURITY.md /src/.oss-scanner/security-policy.md
