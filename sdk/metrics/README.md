# OpenTelemetry Metrics SDK

The code in this directory is currently the legacy impelmentation of the previous experimental metrics SDK specification.


The following set of known issues will be fixed aas the new SDK specification stabilizes:

- The names of SDK insturments do not line up with API instruments.
- Baggage / Context are not available to metrics / views.
- The View API still uses the term LabelsProcessor.
- Only one exporter is allowed.
- Histograms are generating summaries.
- Exemplars are not sampled
- The set of Aggregators goes well beyond the expected "stable" list and (likely) will have some moved to extensions.
- There is no exposed `MetricProcessor` interface.