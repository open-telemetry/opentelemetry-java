window.BENCHMARK_DATA = {
  "lastUpdate": 1696009443731,
  "repoUrl": "https://github.com/open-telemetry/opentelemetry-java",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "name": "jack-berg",
            "username": "jack-berg",
            "email": "34418638+jack-berg@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "bdeb1e95cd24748523592877d82d64212496e96e",
          "message": "Add breedx-splk as approver (#5860)",
          "timestamp": "2023-09-28T16:00:02Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bdeb1e95cd24748523592877d82d64212496e96e"
        },
        "date": 1695922923702,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7042.383864399045,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8119.3603425417705,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13496.410050425166,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16360.254936071939,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13790.273480879177,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687795.7752821383,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.927989433703,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "29139614+renovate[bot]@users.noreply.github.com",
            "name": "renovate[bot]",
            "username": "renovate[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c66a51c4682ee4784168f23029c4421434066f4a",
          "message": "Update dependency io.netty:netty-bom to v4.1.99.Final (#5845)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-09-29T11:56:19-05:00",
          "tree_id": "29a959b127f515a7dfc5a870ea927be9444c0e76",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c66a51c4682ee4784168f23029c4421434066f4a"
        },
        "date": 1696006806988,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7169.314971779628,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8396.29803741356,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12694.15900029212,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15663.078138546713,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16032.165890976106,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686243.6863591715,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8657.592105136986,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "29139614+renovate[bot]@users.noreply.github.com",
            "name": "renovate[bot]",
            "username": "renovate[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bac9941a587582ba43f3cfa7d82bfedcc5e1cb05",
          "message": "Update dependency com.diffplug.spotless:spotless-plugin-gradle to v6.22.0 (#5863)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-09-29T11:56:44-05:00",
          "tree_id": "8e683d61b872638e1cc97eecb4d38dda3f58796b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bac9941a587582ba43f3cfa7d82bfedcc5e1cb05"
        },
        "date": 1696007099349,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7103.651802947787,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8211.994739495969,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12981.143194573548,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16444.72984601983,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15270.019178347886,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684514.0437063545,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8561.242427305964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "29139614+renovate[bot]@users.noreply.github.com",
            "name": "renovate[bot]",
            "username": "renovate[bot]"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bb4c2193aa39ae29aa1af4526a3067d715df9011",
          "message": "Update plugin com.diffplug.spotless to v6.22.0 (#5864)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-09-29T12:39:57-05:00",
          "tree_id": "2bc5962b0395bde7b4f64c2624e74ae15894470a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bb4c2193aa39ae29aa1af4526a3067d715df9011"
        },
        "date": 1696009442835,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7137.227174281769,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 7953.3431531611295,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12368.730017323123,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18164.297425868608,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14522.233940468785,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690107.5867245807,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8621.022618446033,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}