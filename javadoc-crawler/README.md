# Javadoc Crawler

## Context

The javadocs.io website lazy loads content only when the artifacts have been accessed, which can
lead to inaccuracies and confusion when someone loads the
https://www.javadoc.io/doc/io.opentelemetry page, since the published `Latest version` will only be
accurate if someone has accessed the page for the actual latest version.

This module provides a simple scraper that pulls the list of all `io.opentelemetry` artifacts from
maven central and then visits each corresponding page on the javadoc.io website in order to trigger
loading them into the site's system.

See https://github.com/open-telemetry/opentelemetry-java/issues/7294 for more information.

## How to run

```bash
./gradlew :javadoc-crawler:crawl
```
