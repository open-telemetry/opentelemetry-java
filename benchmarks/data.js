window.BENCHMARK_DATA = {
  "lastUpdate": 1712242052015,
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
      },
      {
        "commit": {
          "author": {
            "name": "Josh Suereth",
            "username": "jsuereth",
            "email": "Joshua.Suereth@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5c1bd6cbfe8c8316355ac2db2af86961c14fcd2f",
          "message": "Add adaptable circular buffer implementation for ExponentialCounter. (#4087)\n\n* Add adaptable circular buffer implementation for ExponentialCounter and expose hooks to test its use in Exponential Histogram aggregator.\r\n\r\n* Clean up some adapting circular buffer code.\r\n\r\n* Fix style issues.\r\n\r\n* Apply spotless.\r\n\r\n* Add tests for adapting integer array.\r\n\r\n* Finish wiring ability to remember previous integer cell size and expand testing.\r\n\r\n* Update array copy from code review.\r\n\r\n* Fixes/cleanups from review.\r\n\r\n- Fix a bug in equality where it was forcing ExponentialCounter to have\r\n  the same offset, even if it had stored 0 counts in all buckets. This\r\n  interacts negatively with merge/diff tests where creating a fresh\r\n  exponential bucket would have different indexStart then diff-ing\r\n  another.\r\n- Modify default exponential bucket counter to be adapting circular\r\n  buffer.\r\n- Remove some not-well-though-out methods (like zeroOf, zeroFrom) in\r\n  favor of a \"clear\" method on ExponentialCounter\r\n- Modify ExponentialBucketStrategy to be an actual implementation.\r\n\r\n* Improve testing of copy behavior across exponential-counter implementations.\r\n\r\n* Last fix/cleanup for PR.  Remove remaining TODO around preserving runtime optimisations.\r\n\r\n* Fixes from review.\r\n\r\n* Add test to ensure 0 is returned from exponential counters outside popualted range.\r\n\r\n* Add a bunch of extra equality tests.\r\n\r\n* run spotless.\r\n\r\n* Add note about equality.\r\n\r\n* Add copy() method to AdaptingIntegerArray, update tests.\r\n\r\n* Fix checkstyle.\r\n\r\n* Add internal disclaimer, reduce visibility of test classes\r\n\r\nCo-authored-by: jack-berg <jberg@newrelic.com>",
          "timestamp": "2022-02-04T17:21:04Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5c1bd6cbfe8c8316355ac2db2af86961c14fcd2f"
        },
        "date": 1696022880275,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7204.412237389598,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8333.425527033203,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13198.91341511784,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17648.806632862244,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13688.67884177898,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685819.8162080892,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10795.195730671205,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
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
          "id": "3938d96ce253ed42c2f4a7768c36cb5269acc128",
          "message": "Manual cherrypick for commits 8bbb0e25da6bc8663cbff9ae5975ca20ba229100, b2873186e322d1f3d03d0241aa7a9d160fed4782 (#4104)",
          "timestamp": "2022-01-21T14:35:29Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3938d96ce253ed42c2f4a7768c36cb5269acc128"
        },
        "date": 1696023168762,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7166.016165145586,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8391.494510203867,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12233.716977861079,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15624.720211923637,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13626.953744312148,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687370.8827830453,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10792.649045491282,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Anuraag Agrawal",
            "username": "anuraaga",
            "email": "anuraaga@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "89675582a173dc2df2aa43040decdb2a43ee0989",
          "message": "Use custom resource to avoid version number in assertion string. (#4065)",
          "timestamp": "2022-01-07T05:32:47Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/89675582a173dc2df2aa43040decdb2a43ee0989"
        },
        "date": 1696023401970,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7230.992954151982,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8388.05091967967,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13286.559214182815,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18293.29288409531,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16649.856050925257,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690272.5554244458,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10922.125169022398,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Anuraag Agrawal",
            "username": "anuraaga",
            "email": "anuraaga@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "cb80383df8a3fef56bcffcc011cfc50ff14d6468",
          "message": "Extract SummaryData interface for DoubleSummaryData (#4224)\n\n* Extract SummaryData interface for DoubleSummaryData\r\n\r\n* ValueAtQuantile too\r\n\r\n* javadoc",
          "timestamp": "2022-03-04T08:20:21Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cb80383df8a3fef56bcffcc011cfc50ff14d6468"
        },
        "date": 1696023717949,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7090.040957578092,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8289.06785306184,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13720.698641001984,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16236.855682342353,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14540.014520173707,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684463.7895192255,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10763.104378823966,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Trask Stalnaker",
            "username": "trask",
            "email": "trask.stalnaker@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "4a0962b42f774e9ee5ec5f4951443b138027ab37",
          "message": "Fix release branch version (#4358)",
          "timestamp": "2022-04-08T19:02:48Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4a0962b42f774e9ee5ec5f4951443b138027ab37"
        },
        "date": 1696024035632,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7138.103739542067,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8136.288336048434,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13124.370957722256,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18057.353988603667,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15514.819337261608,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685856.128775043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8594.857500903458,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Java Bot",
            "email": "97938252+opentelemetry-java-bot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "0e0429d0ac37bb25053e468fe708a50006f87a9b",
          "message": "Prepare release 1.14.0 (#4447)",
          "timestamp": "2022-05-09T17:39:47Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0e0429d0ac37bb25053e468fe708a50006f87a9b"
        },
        "date": 1696024313412,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7020.063170579026,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8044.135983993832,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12526.121116552556,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18020.572321457144,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13955.222051675035,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690219.6286484029,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8557.584674765458,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Java Bot",
            "email": "97938252+opentelemetry-java-bot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "ed383f80e82f8b62353e4903472aa7bc5320600b",
          "message": "Prepare release 1.16.0 (#4604)",
          "timestamp": "2022-07-13T02:06:27Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ed383f80e82f8b62353e4903472aa7bc5320600b"
        },
        "date": 1696024573765,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7044.818837724191,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8520.84863938754,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11853.658402030998,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17297.07789052132,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16405.669020288176,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685961.8670305707,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.435994167441,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Java Bot",
            "email": "97938252+opentelemetry-java-bot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "b7779be1542ae4acfcbb99411785ecc15f6ca1cd",
          "message": "Prepare release 1.15.0 (#4522)",
          "timestamp": "2022-06-10T15:58:49Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b7779be1542ae4acfcbb99411785ecc15f6ca1cd"
        },
        "date": 1696024819920,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7088.94927182968,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8550.551221832382,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13659.807141689424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16723.23132553646,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14366.6901839544,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685780.6313254137,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8591.774193621324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Java Bot",
            "email": "97938252+opentelemetry-java-bot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "2d6dfd02588ec0b46440a014387f8589a7d62b06",
          "message": "Prepare release 1.17.0 (#4686)\n\nCo-authored-by: opentelemetrybot <107717825+opentelemetrybot@users.noreply.github.com>",
          "timestamp": "2022-08-12T16:49:48Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2d6dfd02588ec0b46440a014387f8589a7d62b06"
        },
        "date": 1696025154425,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7059.754412953204,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8433.488855909047,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12341.342195956247,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21866.552284451547,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15762.616651478344,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689311.0911105138,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8602.627959526324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Java Bot",
            "email": "97938252+opentelemetry-java-bot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "7b9f9f372074779224aef3dcac5837ca51457798",
          "message": "Prepare release 1.18.0 (#4742)\n\nCo-authored-by: opentelemetrybot <107717825+opentelemetrybot@users.noreply.github.com>",
          "timestamp": "2022-09-09T19:07:46Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7b9f9f372074779224aef3dcac5837ca51457798"
        },
        "date": 1696025464158,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7126.6060305034935,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8363.0934021091,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12028.865475439383,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17260.68997852266,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17891.866408560796,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684027.612163569,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8621.135991369261,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "710587680b17239d072bd3b9bc815c6ce168b09e",
          "message": "[release/v1.21.x] Prepare release 1.21.0 (#5035)\n\n* Prepare release 1.21.0\r\n\r\n* Fix version-dependent test\r\n\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2022-12-09T19:54:41Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/710587680b17239d072bd3b9bc815c6ce168b09e"
        },
        "date": 1696025764824,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7151.555439711781,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8670.931832526332,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12887.494427646072,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15124.888705082418,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16382.846896072195,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689717.7461252422,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8587.969104408241,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "f912f824ae79de6bdc5d302ebac239b4f17df163",
          "message": "Prepare release 1.19.0 (#4824)",
          "timestamp": "2022-10-07T14:27:20Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f912f824ae79de6bdc5d302ebac239b4f17df163"
        },
        "date": 1696026053594,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7169.397187962885,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8105.9617620431645,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11500.973290675329,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17468.925325740885,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15948.537549308287,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688792.0595725677,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8582.479401534625,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "fbeb0d854cbf32b350ffb7fb02ee002d4ae17297",
          "message": "Prepare release 1.22.0 (#5093)",
          "timestamp": "2023-01-06T15:07:58Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fbeb0d854cbf32b350ffb7fb02ee002d4ae17297"
        },
        "date": 1696026347475,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7033.838028063833,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8079.763194995054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13558.748228550243,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16653.259051010577,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14160.243326854878,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688598.4637345045,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8617.944853173454,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "a295066718203267ec45fa8a688da1b2c6e107b8",
          "message": "Prepare release 1.23.0 (#5199)",
          "timestamp": "2023-02-10T20:29:57Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a295066718203267ec45fa8a688da1b2c6e107b8"
        },
        "date": 1696026673822,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7120.594299948856,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8429.81267405753,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12684.794632138339,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17610.47159751478,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13807.498017423179,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686599.7763645288,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8627.41920602134,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "49577ffbde33567d258b588c6567ebb1b798a657",
          "message": "Prepare release 1.23.1 (#5226)",
          "timestamp": "2023-02-15T20:37:21Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/49577ffbde33567d258b588c6567ebb1b798a657"
        },
        "date": 1696026893108,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7198.681573865023,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8265.78573629809,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10614.128166912755,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17002.015261821758,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15356.91458055604,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686783.9280818575,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.755658527465,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
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
          "id": "05b6dfa9f6b7951f31d3f021ed6df3e35d3b8660",
          "message": "Skip build and publish step to finish release (#5283)",
          "timestamp": "2023-03-10T17:12:36Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/05b6dfa9f6b7951f31d3f021ed6df3e35d3b8660"
        },
        "date": 1696027224607,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7098.201421695415,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8264.825374858348,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13108.53442608683,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16870.747455270226,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16330.867132476778,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689810.4723520507,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8568.440719498962,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "cea8cf11d6914a35cdd3eb9bd85ebd054439216f",
          "message": "[release/v1.25.x] Prepare release 1.25.0 (#5358)",
          "timestamp": "2023-04-07T14:41:42Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cea8cf11d6914a35cdd3eb9bd85ebd054439216f"
        },
        "date": 1696027525486,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7073.629200577961,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8504.293201067743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13212.06050978501,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16803.045987897967,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16033.886957952385,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689151.7798097045,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8137.419568792165,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "22fdec25d213adba0aa180e979714742e358b3f6",
          "message": "[release/v1.26.x] Prepare release 1.26.0 (#5426)",
          "timestamp": "2023-05-05T19:35:51Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/22fdec25d213adba0aa180e979714742e358b3f6"
        },
        "date": 1696027826183,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7170.430812853113,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8397.367845147954,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13311.16025490926,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16176.100405204133,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16246.182471247055,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689201.8050383113,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8600.532225161673,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "5a882c8a0085246bd0c883a26cb4fd7c0ee37339",
          "message": "[release/v1.27.x] Prepare release 1.27.0 (#5521)",
          "timestamp": "2023-06-09T16:53:53Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5a882c8a0085246bd0c883a26cb4fd7c0ee37339"
        },
        "date": 1696028130882,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7039.877068244718,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8672.754418651393,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12704.646637637852,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16429.19033504768,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14906.123923191251,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688525.9507707655,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8604.544333089358,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "1733d2cfd76b7808605b62fc60f87f970e7e0b22",
          "message": "[release/v1.28.x] Prepare release 1.28.0 (#5608)",
          "timestamp": "2023-07-07T12:22:23Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1733d2cfd76b7808605b62fc60f87f970e7e0b22"
        },
        "date": 1696028466757,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7124.955473658671,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8249.656339629497,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10739.602271865775,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17924.41202552987,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16822.3783935394,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688680.7303102263,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.813017040404,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "48f13cae7d764fea85d440ec97e027f5cc461824",
          "message": "[release/v1.30.x] Prepare release 1.30.0 (#5801)",
          "timestamp": "2023-09-08T16:28:45Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/48f13cae7d764fea85d440ec97e027f5cc461824"
        },
        "date": 1696028764342,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7154.3602016397235,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8587.912063566815,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12807.055205496972,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18890.934086935544,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15424.549389795362,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683166.6172268249,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8593.119528974237,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "60c06dbe682f686f1a13907604f93d7adeecbef9",
          "message": "[release/v1.30.x] Prepare release 1.30.1 (#5814)",
          "timestamp": "2023-09-11T15:59:56Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/60c06dbe682f686f1a13907604f93d7adeecbef9"
        },
        "date": 1696029008541,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7102.103487620163,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8630.992553867081,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13456.345737381745,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17623.655150439707,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13638.291126404434,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685505.8477622315,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8579.428711349534,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "John Watson",
            "username": "jkwatson",
            "email": "jkwatson@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "33094f4cb14a017f0b1312988c9ee8ab51412876",
          "message": "Update the CHANGELOG.md for the upcoming 1.7.0 release (#3707)\n\n* Update the CHANGELOG.md for the upcoming 1.7.0 release\r\n\r\n* formatting\r\n\r\n* updates for metric name changes and deprecation of the proto module",
          "timestamp": "2021-10-08T15:17:38Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/33094f4cb14a017f0b1312988c9ee8ab51412876"
        },
        "date": 1696029346813,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7165.369485379228,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8040.048387750954,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10918.923232980216,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17818.33327684344,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17287.166990812817,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 565954.7239160243,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10719.081407549867,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "John Watson",
            "username": "jkwatson",
            "email": "jkwatson@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "12225fecaabb78fec156f0bbd4bca9d22f030b87",
          "message": "Update the CHANGELOG for the upcoming 1.6.0 release (#3601)\n\n* Update the CHANGELOG for the upcoming 1.6.0 release\r\n\r\n* add a late-breaking item",
          "timestamp": "2021-09-11T15:15:28Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/12225fecaabb78fec156f0bbd4bca9d22f030b87"
        },
        "date": 1696029728969,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7016.102874474975,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8431.961959937318,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13525.130846109005,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17911.035221427384,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14569.10836463992,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 567217.5462010694,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10716.20722282587,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot",
            "email": "107717825+opentelemetrybot@users.noreply.github.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "cbb44502c1ae4f85bbb28df86c408214e2eda219",
          "message": "[release/v1.29.x] Prepare release 1.29.0 (#5705)",
          "timestamp": "2023-08-11T16:15:20Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cbb44502c1ae4f85bbb28df86c408214e2eda219"
        },
        "date": 1696030054105,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7183.934752049216,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8338.183486288886,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13042.576209622759,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16795.787539701956,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15084.542323364183,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685103.0464896567,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8584.335323935968,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "name": "Anuraag Agrawal",
            "username": "anuraaga",
            "email": "anuraaga@gmail.com"
          },
          "committer": {
            "name": "GitHub",
            "username": "web-flow",
            "email": "noreply@github.com"
          },
          "id": "8587b331b0294b741d3e7a6bcdedb8936b5d40ca",
          "message": "Update diffs (#3821)",
          "timestamp": "2021-11-03T15:25:30Z",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8587b331b0294b741d3e7a6bcdedb8936b5d40ca"
        },
        "date": 1696030271230,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7050.999847315755,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8366.424843628642,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12807.921148538357,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16496.874365168555,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14808.719683013134,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 567971.879344788,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 10720.965353226022,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7e67a84ed31edeac5dbd1ba683df3e1c44f17f22",
          "message": "Drop NaN measurements to metric instruments (#5859)",
          "timestamp": "2023-10-02T10:17:27-05:00",
          "tree_id": "4e76c4c96a80de48193037607dd947034b2bc22e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7e67a84ed31edeac5dbd1ba683df3e1c44f17f22"
        },
        "date": 1696260073652,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 6986.599205506041,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8211.607454482828,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13373.791941634943,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17353.60477026458,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 18100.838118569245,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685782.8837407518,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8628.729785152833,
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
          "id": "1ef437785aa31f7e24ce4698ff2397b7f2f531c4",
          "message": "Update dependency com.squareup.okio:okio-bom to v3.6.0 (#5869)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-02T10:17:41-05:00",
          "tree_id": "dd914854948710c852d3c9f4ff094fb5f522f325",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1ef437785aa31f7e24ce4698ff2397b7f2f531c4"
        },
        "date": 1696260299212,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7209.542521379112,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8396.088349576734,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12609.97458225957,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16126.132871333455,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15406.657349731153,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683353.7781456958,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8602.606473593805,
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
          "id": "5ccc7fccb3fd0bdfa902bfea7bcee43384cdcf3e",
          "message": "Update dependency checkstyle to v10.12.4 (#5868)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-02T10:42:36-05:00",
          "tree_id": "19c8009c81553d498f30e515ddc7ccf09cf23ee6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5ccc7fccb3fd0bdfa902bfea7bcee43384cdcf3e"
        },
        "date": 1696261628916,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7097.986228475987,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8566.212430216536,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13327.730646468994,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17511.93609838651,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15905.37416531982,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675074.898063935,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8563.651300884865,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7310b90530ea33eaf1f1a246140dbcc877f50805",
          "message": "Ignore javadoc.io links (#5878)",
          "timestamp": "2023-10-05T13:34:19-05:00",
          "tree_id": "56afeb19d07080010419afa5959b035725901eef",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7310b90530ea33eaf1f1a246140dbcc877f50805"
        },
        "date": 1696531196123,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7104.583648731515,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8623.340014333338,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12546.70541551523,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17580.22080689019,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15619.917319661385,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691084.717506338,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8633.020709433338,
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
          "id": "1ad621b3f0dc6c5a8e6fde0dbe78585bb4558e9e",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.1 (#5871)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-05T13:43:24-05:00",
          "tree_id": "584dd9b04f42196d28f6aade5062914c6199b15f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1ad621b3f0dc6c5a8e6fde0dbe78585bb4558e9e"
        },
        "date": 1696531628829,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7240.0770029094,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8295.867126098305,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12805.373500795382,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17309.545377570666,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17063.57070578772,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685581.4278354724,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.24618947324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "tylerbenson@gmail.com",
            "name": "Tyler Benson",
            "username": "tylerbenson"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "847d117db1854566b15941f36bf6a02abdc3eb5d",
          "message": "Move data dir to `benchmarks` (#5874)",
          "timestamp": "2023-10-05T14:16:58-05:00",
          "tree_id": "1ebfc98e71de9ac5e40fc424f31c04be6135d69c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/847d117db1854566b15941f36bf6a02abdc3eb5d"
        },
        "date": 1696533981205,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7066.97469838854,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8696.913245400274,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13690.835525446295,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16354.313332583279,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14795.590978122116,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691564.2136502054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8625.96471814628,
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
          "id": "80201515e93757902877dcb5ff737fe0f4e61008",
          "message": "Update dependency gradle to v8.4 (#5881)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-05T14:18:07-05:00",
          "tree_id": "1a0e54db61458c8c11d2794265d75869646c1441",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/80201515e93757902877dcb5ff737fe0f4e61008"
        },
        "date": 1696534308870,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7122.40168381816,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8496.280396642353,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13293.666186209553,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17682.45062760823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15355.420157285072,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678564.802142787,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8594.477037087803,
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
          "id": "4addd7f4a129305b32a104ee2b1758efe0286cc6",
          "message": "Update plugin com.gradle.enterprise to v3.15.1 (#5882)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-05T14:18:26-05:00",
          "tree_id": "c7cd0b5e86a3e8d04bd8956e1da488b19c916710",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4addd7f4a129305b32a104ee2b1758efe0286cc6"
        },
        "date": 1696534602347,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7167.022117781434,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8387.670535664443,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11432.092528265614,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17098.656620736354,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15209.635591876073,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684879.1797505566,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8541.404981989408,
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
          "id": "c39ebfee25e32754f85afb61724d3bc8f0e74ba2",
          "message": "Update dependency com.google.protobuf:protobuf-bom to v3.24.4 (#5879)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-05T14:18:45-05:00",
          "tree_id": "7f3b3f457f3552cacde5411b338fdca316c0c55f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c39ebfee25e32754f85afb61724d3bc8f0e74ba2"
        },
        "date": 1696534851366,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7133.981653061991,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8659.494118937717,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13455.000343919566,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18608.87669977054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17580.62750992155,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685355.3228893988,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8578.318629961364,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "533c30ae02ed99425beec75d1319902178df6942",
          "message": "Fix flaky MetricExporterConfigurationTest (#5877)",
          "timestamp": "2023-10-05T14:19:18-05:00",
          "tree_id": "f49a4df54fca4d62ac945107df613750dd77760a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/533c30ae02ed99425beec75d1319902178df6942"
        },
        "date": 1696535180539,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7165.374271349589,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8604.474163385863,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12854.674435991845,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16836.69100379358,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 11722.118838769995,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690312.7370389025,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8639.938675014955,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "92abcb823b0d13301cc0064520c0a07f4e55091e",
          "message": "Refactor advice API (#5848)",
          "timestamp": "2023-10-06T13:33:10-05:00",
          "tree_id": "47aee1d3c820b42aa502e1e776ccb2f4c9a12861",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/92abcb823b0d13301cc0064520c0a07f4e55091e"
        },
        "date": 1696617907182,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7165.864771360265,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8643.600837856979,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12843.112853862543,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16118.022294609196,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15022.411565913519,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686509.25595484,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8599.406612544075,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bf37a4ca58c1969756848f48f97a36015efa0b47",
          "message": "Prepare 1.31.0 (#5888)",
          "timestamp": "2023-10-06T15:10:48-05:00",
          "tree_id": "8a879b092543b4a13f95204dc55be4c7974a458e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bf37a4ca58c1969756848f48f97a36015efa0b47"
        },
        "date": 1696624005032,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7141.351616580756,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8601.274750511306,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12045.370343878814,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16108.031069087669,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17927.55900430165,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689443.823792755,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8606.897868320459,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "905184fb95e3a34f97ec39a6abfd1f19505cf35c",
          "message": "Update version to 1.32.0 (#5890)",
          "timestamp": "2023-10-06T15:51:00-05:00",
          "tree_id": "7f4a75e8886ca68efa5a0da9f718152a6ea256c3",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/905184fb95e3a34f97ec39a6abfd1f19505cf35c"
        },
        "date": 1696625897214,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7114.514556350127,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8457.537377096849,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12616.761023832318,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17343.7457232235,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16729.27376975773,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680085.5849816601,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8575.704332820209,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3c0bf72a5ebd66d45933d5886cfd0e444acae410",
          "message": "Post release 1.31.0 (#5892)",
          "timestamp": "2023-10-09T09:24:11-05:00",
          "tree_id": "05f3ac8b18b7f10f020cee582226dfc2624b9888",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3c0bf72a5ebd66d45933d5886cfd0e444acae410"
        },
        "date": 1696861700986,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7075.785252174861,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8380.065941464249,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13245.480852462966,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21350.41113184361,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15372.27544144966,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690491.0722793606,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8627.83776998957,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ef3fef9f42d69243ac857c3e06dfc8b342d50039",
          "message": "Add file configuration to autoconfigure (#5831)",
          "timestamp": "2023-10-09T09:41:26-05:00",
          "tree_id": "d6a25f81dedfd0c2d772ffc72952a3ff2e884346",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ef3fef9f42d69243ac857c3e06dfc8b342d50039"
        },
        "date": 1696863652427,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7221.275887725765,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8021.242205161316,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11881.695070445176,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17752.744968116014,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16339.320587788732,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687807.9400057513,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.137121551381,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1523dedb555b033adce27475863e1464b1fb76cf",
          "message": "Update to opentelemetry-configuration v0.1.0 (#5899)",
          "timestamp": "2023-10-11T10:24:32-05:00",
          "tree_id": "a510061ab36ab4060257acdc97dffa2869f615ba",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1523dedb555b033adce27475863e1464b1fb76cf"
        },
        "date": 1697038160001,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7152.787786254599,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8306.856260809525,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12368.941734429585,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18001.373457018875,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15221.019201276082,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687297.1929016754,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8644.141355717922,
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
          "id": "b60eff369862031f111b2fcd880027a0e8cc67d5",
          "message": "Update dependency com.google.guava:guava-bom to v32.1.3-jre (#5901)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-11T13:20:26-05:00",
          "tree_id": "8117ed4ab19604964bdd8926ec2ded7ee0b55e60",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b60eff369862031f111b2fcd880027a0e8cc67d5"
        },
        "date": 1697049028366,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7209.770821239145,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8393.022518064832,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12845.53291639345,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16211.203720338843,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14954.852697475439,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686064.1879936953,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8626.54778829901,
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
          "id": "311fc0a7bec42810765e2f90089abffc3e8e74a6",
          "message": "Update dependency com.google.guava:guava to v32.1.3-jre (#5900)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-11T13:20:53-05:00",
          "tree_id": "ac44363f065d38982cd66bd5746ef566d3c97341",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/311fc0a7bec42810765e2f90089abffc3e8e74a6"
        },
        "date": 1697049317148,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7170.749927440108,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8485.584622636788,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12830.71410817036,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17331.51329921256,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17179.520818244862,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684900.1293714629,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.41050766619,
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
          "id": "cc8774fa0fa13dd63c293c9379151e1bed714cc8",
          "message": "Update dependency io.netty:netty-bom to v4.1.100.Final (#5898)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-11T13:21:52-05:00",
          "tree_id": "f828f2f85ae1e03cf2b1c477b9811b371357a296",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cc8774fa0fa13dd63c293c9379151e1bed714cc8"
        },
        "date": 1697049547063,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7082.6051307639755,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8578.631767362236,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12840.497214695402,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18221.812090586824,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14701.170852107805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685430.3580695647,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8626.606860354768,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3c52cdf7f710568881baa13d5f6d502bb941382d",
          "message": "Prometheus exporter: omit empty otel_scope_info and otel_target_info metrics (#5887)",
          "timestamp": "2023-10-11T13:42:06-05:00",
          "tree_id": "ff323ff23aec236eb1c6d9ac9bea1d6e303c81f4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3c52cdf7f710568881baa13d5f6d502bb941382d"
        },
        "date": 1697050244903,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7120.86534255654,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8194.562231824351,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13360.166852433835,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18167.404274701992,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13575.922937936562,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687620.8157414552,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.11430906771,
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
          "id": "3827ee1e5b9dd34db715a0f1675f59e40382f611",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.26.0 (#5894)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-11T14:05:39-05:00",
          "tree_id": "798430f37d9ad4eb8fbe2cf44202db2b0e4850c2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3827ee1e5b9dd34db715a0f1675f59e40382f611"
        },
        "date": 1697051488194,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7141.691899496126,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8390.61863925948,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13307.880316286028,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18514.19327529151,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15018.528443360216,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690802.1534708664,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8572.45317663894,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8b7780b7c464f640e48818a5bf3c25bd5abb610e",
          "message": "Remove obsolete comment (#5909)",
          "timestamp": "2023-10-12T09:50:32-05:00",
          "tree_id": "f516dd8da30bb9c9bca7e6ad3a04ba97f6921664",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8b7780b7c464f640e48818a5bf3c25bd5abb610e"
        },
        "date": 1697122487505,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7262.548015967579,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8569.617507044688,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12618.322315969848,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16887.760667496088,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14898.089728401828,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686258.8267335787,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8572.321576722565,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ae770ec442cf861733d23a8c869530fc8693c3b4",
          "message": "Enable checking javadoc.io links (#5908)",
          "timestamp": "2023-10-12T09:50:46-05:00",
          "tree_id": "46763f56255a5dc3530e47a53228d91547a85c2e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ae770ec442cf861733d23a8c869530fc8693c3b4"
        },
        "date": 1697122704855,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7200.493600475473,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8064.56778375649,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12341.674796315196,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17696.47289827193,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16292.288040547855,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687983.264837247,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8628.16264638815,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6bc24f4263811979249a94e6f5ca1ec5fe300b39",
          "message": "Stop setting Resource schemaUrl in autoconfigure (#5911)",
          "timestamp": "2023-10-13T10:36:29-05:00",
          "tree_id": "0e294254e2f4f4a13918d38c9096899aff39fffb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6bc24f4263811979249a94e6f5ca1ec5fe300b39"
        },
        "date": 1697212636832,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7092.901254809263,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8273.662158761352,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13044.634473319744,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18190.735841947113,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15836.091478104698,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687729.8216728866,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.802728009792,
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
          "id": "1453f3f1814a56c2719a089b4c96cb173f486e22",
          "message": "Update dependency com.fasterxml.jackson:jackson-bom to v2.15.3 (#5913)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-13T10:36:49-05:00",
          "tree_id": "cc6b36f31eb9770f66a82216ba42058109f917cb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1453f3f1814a56c2719a089b4c96cb173f486e22"
        },
        "date": 1697212862132,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7148.366385211999,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8501.877401560203,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12194.439769292921,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16180.603401797449,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15002.127487388474,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688165.8183538015,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.749065615153,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a9da20f831d215f6653c567946d405d2e0f90971",
          "message": "Cleanup opencensus shim (#5858)",
          "timestamp": "2023-10-13T10:50:12-05:00",
          "tree_id": "d6a5ef29b4fba0d641bc014ec9f3b16d970d4763",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a9da20f831d215f6653c567946d405d2e0f90971"
        },
        "date": 1697213256709,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7057.003749358511,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8521.915582479456,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 14022.387237156197,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15925.66028701096,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15352.932236571938,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686788.0328063767,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8618.45351939158,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "57d83344178f578eb5d2f043b0ffc5c42b6719a5",
          "message": "Dismantle AbstractInstrumentBuilder inheritance hierarchy (#5820)",
          "timestamp": "2023-10-13T14:24:05-05:00",
          "tree_id": "12828960955af3c576a945acef7ea100f20c0924",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/57d83344178f578eb5d2f043b0ffc5c42b6719a5"
        },
        "date": 1697225280643,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7027.769357571303,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8251.876483905597,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12752.401466731115,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17288.45334598233,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13546.70926633227,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682092.3161163065,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8577.32896143238,
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
          "id": "1bff69a663289f74a3fbc45c2e76d54bcceaed80",
          "message": "Update dependency jacoco to v0.8.11 (#5919)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-19T09:22:44-05:00",
          "tree_id": "99fadfa79fa25f473e5c47f18d31832c1fe3d11d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1bff69a663289f74a3fbc45c2e76d54bcceaed80"
        },
        "date": 1697725636881,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7123.493600855798,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8068.11909384969,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12770.75712807018,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19102.3263507187,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13733.489983357835,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678603.6653858996,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8464.579444988505,
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
          "id": "a77e1f9af39e301554f97523abd7414e7f373f55",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.15 (#5929)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-19T09:26:55-05:00",
          "tree_id": "d6ecf889e7c7fa05388d162da0d5d4a7f97eef5a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a77e1f9af39e301554f97523abd7414e7f373f55"
        },
        "date": 1697725883673,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7199.314890709019,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8182.203862865885,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12622.880105860724,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18237.098079447587,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16279.586726632697,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686055.3114936356,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8576.394453581392,
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
          "id": "5740c3ceaf115dd5f19f0bc7e781952fb2d7ad63",
          "message": "Update dependency com.squareup.okhttp3:okhttp-bom to v4.12.0 (#5921)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-19T10:06:04-05:00",
          "tree_id": "2ff950ce9f14353e331ad29497e14fcfc68a5fcd",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5740c3ceaf115dd5f19f0bc7e781952fb2d7ad63"
        },
        "date": 1697728187983,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7167.370686120383,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8548.98228760443,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13026.563720017202,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17393.14922662964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17768.874476060817,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689809.7054705396,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8595.894254704477,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e592d5dbfded93010c871c858c6eac6eff345831",
          "message": "Add env var substitution support to file configuration (#5914)",
          "timestamp": "2023-10-23T10:29:01-05:00",
          "tree_id": "7ccadc877786ef7695795ac24d7a72e2286a0deb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e592d5dbfded93010c871c858c6eac6eff345831"
        },
        "date": 1698075159603,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7140.504049203694,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8141.7465182325595,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12530.192642260674,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17768.177801261758,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16420.09131858714,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686780.932890823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.514040117108,
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
          "id": "70cb2c9235430fd8c9d88e4875444b1c11dc3cf9",
          "message": "Update dependency org.owasp:dependency-check-gradle to v8.4.2 (#5935)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-23T10:29:37-05:00",
          "tree_id": "4eeb1c365360cceb26040dd2bff621bd95adc750",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/70cb2c9235430fd8c9d88e4875444b1c11dc3cf9"
        },
        "date": 1698075447127,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7110.79986674733,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8155.935336322992,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13318.77071954025,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17439.282996225476,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16087.480727338198,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678060.1761405405,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8575.768563875348,
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
          "id": "c3a2e94579caaf6b35cf5902cd8a79f58d9a9e15",
          "message": "Update dependency io.grpc:grpc-bom to v1.59.0 (#5934)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-23T10:29:49-05:00",
          "tree_id": "8b2c4f0b92d011fec0c694db39751604646fe941",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c3a2e94579caaf6b35cf5902cd8a79f58d9a9e15"
        },
        "date": 1698075679393,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7205.969577379263,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8247.331669007728,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13392.77560168805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17453.361935849483,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14643.656520265165,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688173.303575876,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8579.288074500022,
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
          "id": "cb44b2b18c474f80478184ff1665bc3fa5d2ced3",
          "message": "Update errorProneVersion to v2.23.0 (#5927)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2023-10-23T10:31:09-05:00",
          "tree_id": "eefa562c93d34d64bc0e84559161647020dbe48f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cb44b2b18c474f80478184ff1665bc3fa5d2ced3"
        },
        "date": 1698075928789,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7108.190951580574,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8354.581363746125,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13452.288130405732,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18872.123467795118,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13882.584169419424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681002.337085941,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.382614330312,
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
          "id": "82a0e03add880ecfe1b6ad8b72bccbd393a585cb",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.27.0 (#5933)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-23T11:03:10-05:00",
          "tree_id": "281eca3ab5aa1661ff17762e4d9890d8b4873948",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/82a0e03add880ecfe1b6ad8b72bccbd393a585cb"
        },
        "date": 1698079654984,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7252.721778332178,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8286.359307828898,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12492.412375627315,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16600.857454040517,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15221.458443159649,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688070.5350468687,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8640.97660596628,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e06d35add577fd702104b5b2836201e76f72d88b",
          "message": "JdkHttpSender should retry on connect exceptions (#5867)",
          "timestamp": "2023-10-23T11:03:50-05:00",
          "tree_id": "945c41e9de2f8a3eb74063688db46bbc5cdbd63e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e06d35add577fd702104b5b2836201e76f72d88b"
        },
        "date": 1698079876754,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7113.85463796162,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8329.101245174601,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12913.084895971759,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18172.24650172299,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16206.241353531277,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687603.0584072634,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8609.546042744245,
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
          "id": "6c931951b3a8e7a4d7250e793f72372e330c2550",
          "message": "Update dependency me.champeau.jmh:jmh-gradle-plugin to v0.7.2 (#5946)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-27T14:45:28-05:00",
          "tree_id": "98713e539a00ec828d7d66bdf98e907ad3b80cb8",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6c931951b3a8e7a4d7250e793f72372e330c2550"
        },
        "date": 1698438338502,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7138.812147303646,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8563.8528527409,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12331.424168822876,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15676.870952784268,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15369.063249438626,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686333.5911656697,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8578.41866325819,
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
          "id": "976edfde504193f84d19936b97e2eb8d8cf060e2",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.26.0 (#5943)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-27T14:45:48-05:00",
          "tree_id": "f6c724e920034ac20638c1ac1226a440b2368189",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/976edfde504193f84d19936b97e2eb8d8cf060e2"
        },
        "date": 1698438563166,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7100.444686051671,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8599.946027587808,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13528.694690785665,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17409.808282634105,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16370.941984457526,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690371.5667421569,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.6949160837,
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
          "distinct": false,
          "id": "33d3a9213c38feaa8e44e07e52087a23c2b327c2",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.26.1 (#5947)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-10-30T16:52:19-05:00",
          "tree_id": "5f73307d7773b2a6a460d3bb12dd3ddbf681a934",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/33d3a9213c38feaa8e44e07e52087a23c2b327c2"
        },
        "date": 1698704268301,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7096.230029805113,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8458.115664699917,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12744.155240477856,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16470.012576868136,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16525.163741822034,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686455.0090544266,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.55680359268,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "abdolsamad@gmail.com",
            "name": "Abdolsamad Montazeri Shahtoori",
            "username": "abdolsamad"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ccb2e04237242b44e368524a41954941e2a8fe28",
          "message": "Fix opentracing header name issue (#5840)",
          "timestamp": "2023-10-31T13:30:34-05:00",
          "tree_id": "c3f352287651dd031f3889fdb2cef73d88271803",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ccb2e04237242b44e368524a41954941e2a8fe28"
        },
        "date": 1698777251336,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7077.608274395759,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8480.422124735856,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13361.336525108043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16707.632075443864,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16417.377649550275,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684325.701556322,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8469.074488888918,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "efa46a5dccb962b50943a0b4905c1800282d6be6",
          "message": "Experimental support for Log AnyValue body (#5880)",
          "timestamp": "2023-10-31T15:05:36-05:00",
          "tree_id": "a3be49ec9218feadfdfb10ee9b3c459e76fefeb8",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/efa46a5dccb962b50943a0b4905c1800282d6be6"
        },
        "date": 1698784230461,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7062.540590549365,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8503.971056630788,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13040.271761633388,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18953.41446639186,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13500.14959370023,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680578.3275184631,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8592.947533532535,
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
          "id": "9d55f25386c2186a30efe8f0054b8dcb5eb0a0bd",
          "message": "Update dependency com.google.protobuf:protobuf-bom to v3.25.0 (#5957)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-03T16:19:01-05:00",
          "tree_id": "07d7c85c4eab9d74825166f5a55e80c767a8733a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9d55f25386c2186a30efe8f0054b8dcb5eb0a0bd"
        },
        "date": 1699046592189,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7081.12023034091,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8386.240847462257,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13272.902782520663,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18886.62967460531,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16595.836492686813,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1679679.0382799278,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8562.789596571198,
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
          "id": "35ecf7a46128257c7d6106ea22d78b0207612245",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.16 (#5956)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-03T16:19:21-05:00",
          "tree_id": "2b45087f777eef94825919613cb65e7eac2713a2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/35ecf7a46128257c7d6106ea22d78b0207612245"
        },
        "date": 1699046840848,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7188.364123791146,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8088.073110344386,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11879.174225772966,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 14542.187829854962,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21218.140743348973,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1679410.182311289,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8593.751788489742,
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
          "id": "0263e62b4226762d3beb4e4a4d529d023f8359cc",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.3 (#5953)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-03T16:19:41-05:00",
          "tree_id": "9eb4f229eed2331e6ad2e133838fc8a2caf30ac6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0263e62b4226762d3beb4e4a4d529d023f8359cc"
        },
        "date": 1699047066591,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7095.549123139671,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8690.942308127649,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13477.477839542722,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16407.13701475273,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16109.543697954645,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689251.3993773765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8610.312532108508,
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
          "id": "c0cf5dd95fa417040819dd9509a26bb18e37f022",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.28.0 (#5951)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-03T16:20:43-05:00",
          "tree_id": "1f82f54d62836fba15b362192a7f9bc9b5703410",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c0cf5dd95fa417040819dd9509a26bb18e37f022"
        },
        "date": 1699047287156,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7224.330774323583,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8645.624840315395,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12529.141089879526,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16871.964212953368,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13658.84023580206,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688046.482393035,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8577.942950009281,
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
          "id": "e2c76320ed05b628c74e94493b7312817e92647a",
          "message": "Update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v1.9.20 (#5949)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-03T16:21:01-05:00",
          "tree_id": "6df45ad8a8393866c78ee1803fdcab3b1e456937",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e2c76320ed05b628c74e94493b7312817e92647a"
        },
        "date": 1699047582660,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7161.350952330636,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8529.737650063162,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12035.165498892053,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18524.889709166477,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16419.696352202765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684176.5412006236,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8572.027035099292,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7fd46f058350f35cb222eec1f76153b3751121b8",
          "message": "Fix flaky jaeger remote sampler test (#5955)",
          "timestamp": "2023-11-03T16:26:01-05:00",
          "tree_id": "03ecbb168682c2ba5983046f29145cc353e272c3",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7fd46f058350f35cb222eec1f76153b3751121b8"
        },
        "date": 1699047800162,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7176.957108759206,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8777.778964618405,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13252.415256878729,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17375.28927812964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14703.338364360554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686879.9842635598,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8631.638845606803,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "19196a02519cf83ee0867f58c7e9532d3ed29c55",
          "message": "Stabilize explicit bucket boundaries advice API (#5897)",
          "timestamp": "2023-11-06T10:58:34-06:00",
          "tree_id": "49d4066d9784180277a8670faf4b4692759b1963",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/19196a02519cf83ee0867f58c7e9532d3ed29c55"
        },
        "date": 1699290163382,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7195.404961724036,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8564.431274672743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12914.899417390496,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16189.410796337503,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15431.838923596917,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680927.5086227853,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8562.640940177162,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1ecc9197bea8a566bf4ec6e3afa6b70f55d593ef",
          "message": "Add null check to StrictContextStorage (#5954)",
          "timestamp": "2023-11-06T11:12:29-06:00",
          "tree_id": "d9e7ba38a6e7a4ac0f7dc9a36981f04bdaa621de",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1ecc9197bea8a566bf4ec6e3afa6b70f55d593ef"
        },
        "date": 1699290967987,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7094.503714758327,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8651.119168471805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12599.849412167721,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17036.642003208184,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13748.849017288827,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688929.811925944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.008355330001,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f9be6821a53d51797d73b16975fe405232a39c6f",
          "message": "Use a custom output stream in marshaling jmh benchmarks (#5964)",
          "timestamp": "2023-11-07T10:01:41-06:00",
          "tree_id": "fd1040abcab4586d743f5fd65ab8b140a0e91aba",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f9be6821a53d51797d73b16975fe405232a39c6f"
        },
        "date": 1699373118804,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7210.337515831862,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8472.530547181595,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11567.990662443659,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17344.592644677054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16374.222833969445,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686436.966788012,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8602.880815406663,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "56847527+LikeTheSalad@users.noreply.github.com",
            "name": "César",
            "username": "LikeTheSalad"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f99e4961cb91447f242f3cd044acf0531b4c8bd3",
          "message": "Identifying otel http calls (#5918)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2023-11-09T08:49:06-06:00",
          "tree_id": "e449dcc2a7e11cad8354c61fe8163d1f22eef78a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f99e4961cb91447f242f3cd044acf0531b4c8bd3"
        },
        "date": 1699541588593,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7152.596212606097,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8537.698286604054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12632.361050605334,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16195.178086378237,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 20688.124804248695,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686005.9040157688,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.392988868613,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f2f3ab33b764733b9c70519967b8eabeb4c90063",
          "message": "Autoconfigure listener (#5931)",
          "timestamp": "2023-11-09T08:49:21-06:00",
          "tree_id": "f93baa0f1355886aee1f8e44f180c710ce4f266a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f2f3ab33b764733b9c70519967b8eabeb4c90063"
        },
        "date": 1699541818164,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7096.398281819941,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8347.46797030462,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12887.157971547724,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18168.207514919344,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15341.56570837498,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684914.4528176233,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.960958380208,
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
          "id": "59f3eb8eef3f348ef089d5dbdcb07374ddce9267",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.26.2 (#5968)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-09T09:19:24-06:00",
          "tree_id": "3f3a770ce262da579c86280d505468163741408f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/59f3eb8eef3f348ef089d5dbdcb07374ddce9267"
        },
        "date": 1699543388678,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7188.191920933708,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8394.993995626735,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11430.733946918514,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16096.681650064547,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13850.391161098552,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689892.4033364984,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.871728062819,
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
          "id": "a9dfcd0cf055c4ea284fa401da34b9aba94256c0",
          "message": "Update dependency com.tngtech.archunit:archunit-junit5 to v1.2.0 (#5963)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-09T09:19:45-06:00",
          "tree_id": "5d260e83f05f20462abc97d986f137fcf0ba3b7c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a9dfcd0cf055c4ea284fa401da34b9aba94256c0"
        },
        "date": 1699543608395,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7078.854081378246,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8236.807009001799,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12904.372474247062,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18591.329925304795,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14905.04510471609,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690188.8801248304,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.100765948158,
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
          "id": "efcce14ba98dd23b99b65b60c067e120f44ca79e",
          "message": "Update dependency org.junit:junit-bom to v5.10.1 (#5962)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-09T09:20:15-06:00",
          "tree_id": "c495be1272628804a07355058904f4fd8843bac2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/efcce14ba98dd23b99b65b60c067e120f44ca79e"
        },
        "date": 1699543836047,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7180.559391673114,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8521.772447453908,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12673.101383308378,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17280.194278858646,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16933.54096203547,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684453.7256994746,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8593.238927149463,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b03ec3aa62d8823e2eb3bcd19be6082d53e209b6",
          "message": "Add log support to junit extensions (#5966)",
          "timestamp": "2023-11-09T10:03:36-06:00",
          "tree_id": "0e1d4d28f11030453e08fc9729466827ceb0b85c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b03ec3aa62d8823e2eb3bcd19be6082d53e209b6"
        },
        "date": 1699546041747,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7094.652070983631,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8580.447571964214,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13311.635160413667,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18202.97000682526,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16444.752671059672,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681338.0868734668,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8600.458587831325,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "83993e03d342726bd67870752c96be9af3fd5cc3",
          "message": "Allow events to be emitted with timestamp (#5928)\n\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2023-11-09T15:58:58-06:00",
          "tree_id": "1b44dc2b170771883d726040b17857a302577c38",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/83993e03d342726bd67870752c96be9af3fd5cc3"
        },
        "date": 1699567371018,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7074.882418880125,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8382.925273575715,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12852.145838509066,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17089.111742616158,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16412.599370818778,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683216.5181551122,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8554.820508933766,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "04f6d9cc4600596de75e8dee12991d6cb5e49735",
          "message": "Fix delta metric storage concurrency bug (#5932)",
          "timestamp": "2023-11-10T09:00:56-06:00",
          "tree_id": "a8727f2e1c78e34dda885bb71058b1a293cc1dfa",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/04f6d9cc4600596de75e8dee12991d6cb5e49735"
        },
        "date": 1699630147227,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7186.8422220330785,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8232.21929450054,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12445.787764884872,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17321.299527317347,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15526.650033293374,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684901.51991374,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8615.407877924026,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f44d99108d3c3c0b692f31a30d96cb98eb8e8b69",
          "message": "Expand the set of retryable exceptions in JdkHttpSender (#5942)",
          "timestamp": "2023-11-10T09:30:03-06:00",
          "tree_id": "d40b2838cf773a42fcd1ca6f9cc7d07db589b7e1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f44d99108d3c3c0b692f31a30d96cb98eb8e8b69"
        },
        "date": 1699630802998,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7129.651004559737,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8286.317405724021,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12575.123702559798,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17059.717369894577,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15934.422773401315,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690397.1516987353,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8603.413734580014,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "aca4157d8f2cc39f5b120975c5e352e82284b2e5",
          "message": "Stop publishing announcement on release (#5971)",
          "timestamp": "2023-11-10T11:55:58-06:00",
          "tree_id": "2c45b8fba02f64a387c649478675b11b83e5b5ab",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/aca4157d8f2cc39f5b120975c5e352e82284b2e5"
        },
        "date": 1699639173795,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7107.13532507002,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8540.73780655628,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12697.499649354677,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16658.66563306861,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14139.956888297433,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689206.475510823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.289704416324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "72a5bb151bd18e86defacd1588f98ac9d26b9975",
          "message": "CAS and voltile approach to fix delta concurrency bug (#5976)",
          "timestamp": "2023-11-13T09:43:27-06:00",
          "tree_id": "0813dea197ad3e5329b516fa46c8d977d64c0f28",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/72a5bb151bd18e86defacd1588f98ac9d26b9975"
        },
        "date": 1699892079030,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7161.91603225529,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8293.210934564418,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13089.439114088753,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 22356.446772726606,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14859.21599844179,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690315.7093801983,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8631.764580665975,
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
          "id": "ab4379da25d1264114e5a278c3b1c7fce3220b2a",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.2 (#5978)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-13T11:33:49-06:00",
          "tree_id": "9151231b2f96446deff8f10d5da6c0a60ef5f2ab",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ab4379da25d1264114e5a278c3b1c7fce3220b2a"
        },
        "date": 1699897116723,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7120.132970341085,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8262.262638557793,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13466.61357686717,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18015.42487624815,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16331.064008834883,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1674214.098996772,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8565.212526032694,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "cc5b70c737a96dc71b4984817deb3a1b390884b8",
          "message": "Prepare 1.32.0 (#5977)\n\nCo-authored-by: Trask Stalnaker <trask.stalnaker@gmail.com>",
          "timestamp": "2023-11-13T11:34:01-06:00",
          "tree_id": "8811065111529f3b983931a8f332ed287ac0a914",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cc5b70c737a96dc71b4984817deb3a1b390884b8"
        },
        "date": 1699897336857,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7129.432364204195,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8597.45559058232,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13298.168060510721,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16664.245027113495,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14177.564194797513,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690620.8497231596,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.78608508052,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "320a4d32d83b417d3801380a54285ad9ad01c838",
          "message": "Update version to 1.33.0 (#5979)",
          "timestamp": "2023-11-13T12:04:20-06:00",
          "tree_id": "af63b387e4c2ccc1e5ac41398f4740560eeb3183",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/320a4d32d83b417d3801380a54285ad9ad01c838"
        },
        "date": 1699898902395,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7163.562172955279,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8259.708553794375,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13090.684004064915,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15226.877837289203,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15064.65781247493,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687275.6201325457,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.954533238153,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "89014162077706c649c061ace9f53f4158e26107",
          "message": "Post release 1.32.0 (#5982)",
          "timestamp": "2023-11-13T15:29:11-08:00",
          "tree_id": "d705374bd438696f6f080ec91540d6a11b14fafe",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/89014162077706c649c061ace9f53f4158e26107"
        },
        "date": 1699918368171,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7200.0971896784085,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8201.41674111296,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12331.76615384463,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17429.452685570785,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16726.22038386421,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689844.7349014487,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.568297829375,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9ac678e81bce7c12820acdb846d22d7957b8b15f",
          "message": "Fix SynchronousMetricStorageTest flake (#5981)",
          "timestamp": "2023-11-14T10:23:42-06:00",
          "tree_id": "f706da2dec597a16400a923965ccdd134a249734",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9ac678e81bce7c12820acdb846d22d7957b8b15f"
        },
        "date": 1699986580222,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7037.967796094364,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8572.231608987406,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11688.151592624152,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17989.31516669295,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16323.417021494863,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691406.6252209242,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.145833220433,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "514d0820b2828f82b89fd7aae7f42387cd9fc81a",
          "message": "MetricsBenchmarks should share state between threads (#5984)",
          "timestamp": "2023-11-16T16:53:54-06:00",
          "tree_id": "543a28b44ce6ae99d80f8ea6a5142a1cc08a173e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/514d0820b2828f82b89fd7aae7f42387cd9fc81a"
        },
        "date": 1700177781980,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7085.184457869583,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8234.679682697151,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12985.316162938123,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19085.476482440135,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15910.571692224583,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683324.589518567,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8552.21842998858,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "531898cd6b906a2273291f7683e15e04212d8161",
          "message": "Define language version compatibility requirements (#5983)",
          "timestamp": "2023-11-16T17:01:23-06:00",
          "tree_id": "3039e1a1bf1c3d66754f700f733ca19d1f6622de",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/531898cd6b906a2273291f7683e15e04212d8161"
        },
        "date": 1700177998958,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7136.298354097581,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8477.96854071026,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12406.692015981549,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17496.952143442606,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15992.61650178821,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688656.4832016502,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8624.171551927142,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "ltulmin@splunk.com",
            "name": "Lauri Tulmin",
            "username": "laurit"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c87852c3d135bd670ab8ad5762809420c5f2dbf7",
          "message": "Target kotlin 1.4 in kotlin extension (#5910)",
          "timestamp": "2023-11-17T13:54:13-06:00",
          "tree_id": "f1b6d612d075b843bbf3467be950b29eafc72b4e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c87852c3d135bd670ab8ad5762809420c5f2dbf7"
        },
        "date": 1700252219468,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7154.340384900026,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8198.930204323098,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13401.4821622121,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18205.202811544765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16375.476423050572,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689429.2552463855,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.040145564106,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "56847527+LikeTheSalad@users.noreply.github.com",
            "name": "César",
            "username": "LikeTheSalad"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f58a209e9acd8edf064c979af7f43d5d005be633",
          "message": "Building animal sniffer signatures directly from android corelib (#5973)",
          "timestamp": "2023-11-20T12:12:55-06:00",
          "tree_id": "b8e49e55e73ae483258e6d9440c333774e28c214",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f58a209e9acd8edf064c979af7f43d5d005be633"
        },
        "date": 1700504257795,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7170.284528781934,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8492.616859286312,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13092.271856560179,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17342.31367277365,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14936.695671094854,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1676272.2020424735,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.652802429973,
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
          "id": "0f60bd421518c1d84fdf545c80a9024c5072aaa8",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.17 (#6000)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:17:44-06:00",
          "tree_id": "53742d4e248710ec276d9949901b11af6060852c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0f60bd421518c1d84fdf545c80a9024c5072aaa8"
        },
        "date": 1700514004011,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7132.100777592887,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8247.346313032533,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13703.56091552964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17397.24573386536,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15024.959214857468,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687018.4415577743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8471.79163330912,
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
          "id": "c7b2c8b00beb9588f17662fb5f4c1d1c9c6529df",
          "message": "Update dependency com.fasterxml.jackson:jackson-bom to v2.16.0 (#5992)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:18:05-06:00",
          "tree_id": "92201d78f06eb4697720ea8b79fe113f679a7f17",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c7b2c8b00beb9588f17662fb5f4c1d1c9c6529df"
        },
        "date": 1700514235796,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7143.946203703201,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8547.099323902034,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12609.434569875148,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16986.611221330157,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14949.541664813343,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682646.2433337823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8588.78805531585,
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
          "id": "8e0f6124cf80681dd428e9587785fe7719dbb8ed",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.26.3 (#5996)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:19:03-06:00",
          "tree_id": "e40fdc349357ae1422d21d55ab51ac27ecf32ef1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8e0f6124cf80681dd428e9587785fe7719dbb8ed"
        },
        "date": 1700514463604,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7319.901073635591,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8520.314664169853,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13759.944088358641,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18638.852631728212,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14532.852286671996,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685768.2245819464,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.928104406627,
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
          "id": "9a01f2e5927012e9a3ed21b7e82593c9de585208",
          "message": "Update dependency org.owasp:dependency-check-gradle to v8.4.3 (#5988)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:19:39-06:00",
          "tree_id": "217f3dc7c09d36b66e4b46bc7e1c7460e881648f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9a01f2e5927012e9a3ed21b7e82593c9de585208"
        },
        "date": 1700514756377,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7119.181370197624,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8442.415307920039,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13672.864698313864,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18857.882292737722,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16808.611131936163,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685161.3192101163,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.511528168066,
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
          "id": "a33de86cb63a68b9ccc7e332ca21fea54731d89e",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.2 (#5989)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:19:58-06:00",
          "tree_id": "6f47f868c90702695e50bef72ff9334ec737e1ff",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a33de86cb63a68b9ccc7e332ca21fea54731d89e"
        },
        "date": 1700514985697,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7245.075128680405,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8375.21594922019,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13081.300958485528,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18420.302576882226,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16044.389506285248,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682515.6763338353,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8631.130047000766,
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
          "id": "34cd3f9342ece98090d72d99b4ee4994ee2f33c0",
          "message": "Update dependency checkstyle to v10.12.5 (#5985)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T14:20:11-06:00",
          "tree_id": "ae6aa6330788530226be9206c002fc224bcf8820",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/34cd3f9342ece98090d72d99b4ee4994ee2f33c0"
        },
        "date": 1700515260997,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7060.1499864329735,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8352.434813984091,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13294.012924874254,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17652.346660879713,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15478.218870486231,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683072.2789717403,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.792031107709,
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
          "id": "e2dd3e4404681d17cdde48a240ac363547c049c6",
          "message": "Update dependency com.google.protobuf:protobuf-bom to v3.25.1 (#5991)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-20T15:35:48-06:00",
          "tree_id": "bbb7862dc4d65a88fa998f37c3c026198f8fe752",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e2dd3e4404681d17cdde48a240ac363547c049c6"
        },
        "date": 1700525010766,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7109.486552239385,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8332.289442230254,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12901.296929269305,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17615.843786860765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16260.882294488005,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686416.034571537,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8592.942399633583,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e6c7a0f264569a514df3caba0b7f59f80c4ba3d2",
          "message": "Base64 encode AnyValue bytes in string representation (#6003)",
          "timestamp": "2023-11-21T10:02:53-06:00",
          "tree_id": "fc625de0871cd49cae3238bd3177ca11830c3031",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e6c7a0f264569a514df3caba0b7f59f80c4ba3d2"
        },
        "date": 1700582803395,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7048.206155389075,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8593.683996659896,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12934.31753138902,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16631.484765736717,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16768.647461700177,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687775.6004501719,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8611.665547261106,
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
          "id": "1e82b72f36d847e743a525ae8767a6badd8deb81",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.3 (#6005)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-21T13:15:23-06:00",
          "tree_id": "a8a6e1b73fec693478b7f7571020d8ba241c7d86",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1e82b72f36d847e743a525ae8767a6badd8deb81"
        },
        "date": 1700594348571,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7262.28781337138,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8602.961678949776,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13331.321099527268,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17459.078501968845,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21741.349507595343,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690776.4520451191,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8624.79329362024,
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
          "id": "ad7fc9e75366747c3984e4480fce02a087bf6b48",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9 (#6007)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-22T14:19:35-06:00",
          "tree_id": "04adb8574a01ab80c539fc73f6adbf6f2dec6a8f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ad7fc9e75366747c3984e4480fce02a087bf6b48"
        },
        "date": 1700685528306,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7073.915970333973,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8650.462069151039,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11629.522197243143,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17139.794782102814,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14950.132090744,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680521.2657237668,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8539.817834733814,
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
          "id": "28ff59de414774a078d0072418c61f4f6d6fd5c2",
          "message": "Update plugin org.graalvm.buildtools.native to v0.9.28 (#5793)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2023-11-23T08:36:18-06:00",
          "tree_id": "5e63d3df70ee8d88ea7725bbb9623b6ed3f1fd6d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/28ff59de414774a078d0072418c61f4f6d6fd5c2"
        },
        "date": 1700751172545,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7060.615858017613,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8580.953891700756,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13213.906420349085,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17542.766976512663,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16518.451207571314,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689025.868443796,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.540132969883,
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
          "id": "675101a4eaf32c585e5af98c53dd2801009ed783",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.3 (#6009)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-27T12:35:02-06:00",
          "tree_id": "0cb8720f323a31d4c7ee14a94123c956fd700e3f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/675101a4eaf32c585e5af98c53dd2801009ed783"
        },
        "date": 1701110392581,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7159.584859527033,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8182.746282404555,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12608.922268517392,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16465.87862068477,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15102.469818330093,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677303.5628694887,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.26293150567,
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
          "id": "3382073239d4a070568f2483766ac22f842326ef",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.1 (#6012)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-27T12:35:23-06:00",
          "tree_id": "4d43014265e1dce9980a1c1400979a8e15832e13",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3382073239d4a070568f2483766ac22f842326ef"
        },
        "date": 1701110685726,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7004.459456923708,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8395.304576640801,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12537.424943336233,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16460.36721971025,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16984.622285791513,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1676229.2004442678,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8558.256158104557,
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
          "id": "1dcea73ea894591e8ec58e883b4860ca9bc34b88",
          "message": "Update dependency org.jctools:jctools-core to v4.0.2 (#6006)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-27T12:36:40-06:00",
          "tree_id": "03b41653806f7e7fecff0b88e4bd872b9659e506",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1dcea73ea894591e8ec58e883b4860ca9bc34b88"
        },
        "date": 1701110914939,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7073.983063993792,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8240.441095776321,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13251.8778077621,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16413.10739453254,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14396.030846894346,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686093.7121379625,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8638.861400221838,
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
          "id": "c57ca65f7f1455e2f9d72f3d2d626aaf2f2f9a81",
          "message": "Update dependency com.toasttab.android:gummy-bears-api-21 to v0.7.0 (#6002)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-27T12:37:25-06:00",
          "tree_id": "921a646561ba4a94d44c24bcb3a3a43843ddb27d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c57ca65f7f1455e2f9d72f3d2d626aaf2f2f9a81"
        },
        "date": 1701111137757,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7150.882688291475,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8488.657050473135,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12430.435170163068,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17859.467672302737,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16277.411474647253,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691112.0731986735,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8650.930524906868,
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
          "id": "f9e0acba15ccbd494b228d69496ebb612ed6db21",
          "message": "Update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v1.9.21 (#6008)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-27T20:14:37-06:00",
          "tree_id": "654c9efda61eba06ed513f43541009f1a84521ea",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f9e0acba15ccbd494b228d69496ebb612ed6db21"
        },
        "date": 1701137978133,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7170.81090385131,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8424.393814638166,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12881.337719585641,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16481.573206135006,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15827.440506257146,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683140.5388494816,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8572.015993115867,
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
          "id": "da2cc9f0479fbc26f0ccbc317cd07c41787698f9",
          "message": "Update dependency io.grpc:grpc-bom to v1.59.1 (#6021)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-28T09:41:54-06:00",
          "tree_id": "4f538b380e9502dca43f5b37115fc9c56561b0fc",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/da2cc9f0479fbc26f0ccbc317cd07c41787698f9"
        },
        "date": 1701190467398,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 6993.674017491386,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8288.971204593157,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13228.211939192994,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18823.49939105388,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15520.128730206527,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689104.6339812265,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8582.571398448988,
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
          "id": "433ad9462d12132d3d74c4d1832cca946ffdd769",
          "message": "Update plugin com.diffplug.spotless to v6.23.2 (#6014)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T10:07:46-06:00",
          "tree_id": "c35fe92818389f8ed25e708efb279a5d3f53fc1f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/433ad9462d12132d3d74c4d1832cca946ffdd769"
        },
        "date": 1701360715644,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7139.417361321694,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8289.742935187742,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13096.536833029608,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17350.467659476977,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16383.577740414508,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688468.8889081306,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8525.991040300438,
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
          "id": "52958c8e91cc85e3b59e4c4fafe97170c5ba48e6",
          "message": "Update dependency gradle to v8.5 (#6029)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T10:09:35-06:00",
          "tree_id": "50eb298ea39ba8296885be604cd1b232c5ec7c45",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/52958c8e91cc85e3b59e4c4fafe97170c5ba48e6"
        },
        "date": 1701361039796,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7203.677666139595,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8690.245373774533,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12878.196055134165,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 22573.131554475993,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17221.5387491905,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682838.1554800333,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8579.863683700662,
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
          "id": "6867bd04bd764bdec6ea13a8dbe00f4030d47bab",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.29.0 (#6032)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T10:10:03-06:00",
          "tree_id": "866bd50cfe7bb9e3de2faa552ceb6d01a8ad0754",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6867bd04bd764bdec6ea13a8dbe00f4030d47bab"
        },
        "date": 1701361259976,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7051.937220936832,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8225.309304637563,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13232.017593317316,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16715.888810230314,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14969.635644406031,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690732.4558383923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8628.51242588078,
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
          "id": "0e9ca2b863e18ae6841eca370bfa8ca24a18cce3",
          "message": "Update actions/setup-java action to v4 (#6030)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T10:11:02-06:00",
          "tree_id": "14976ecf970def0d2321199d900c5215869b1ea4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0e9ca2b863e18ae6841eca370bfa8ca24a18cce3"
        },
        "date": 1701361477208,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7212.225227842617,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8445.857321780355,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13138.115190275115,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18324.336712185464,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21531.93650695745,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689241.57107969,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.971305967212,
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
          "id": "537a4dfc1b160ddeedd056a806f3f2a2e49e8faf",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.4 (#6028)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T10:11:32-06:00",
          "tree_id": "0d032023abac7a760ae05d7d52dca2c6af83f879",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/537a4dfc1b160ddeedd056a806f3f2a2e49e8faf"
        },
        "date": 1701361700331,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7150.090694137072,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8342.997320662267,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13251.541694038378,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17332.489461384623,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16711.836691015356,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687129.071194858,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8611.492292858398,
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
          "id": "14eaa4cdf893f8f65285264ca72918399628f0ef",
          "message": "Update dependency com.diffplug.spotless:spotless-plugin-gradle to v6.23.0 (#6016)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2023-11-30T11:14:59-06:00",
          "tree_id": "96cd186d5c2a4a0268d6d4d6c2313f6058c156ca",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/14eaa4cdf893f8f65285264ca72918399628f0ef"
        },
        "date": 1701366782691,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7073.762822350372,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8429.932881224453,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12419.094702048129,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 22061.528470679692,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16094.39016479332,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683621.7646702055,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8584.508599844243,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "40325e56dd0f339978ed015738b32a4d57e924fe",
          "message": "Remove exception handling in TlsUtil#decodePem (#6034)",
          "timestamp": "2023-11-30T17:00:20-06:00",
          "tree_id": "22483e3fd88a9e7e2cbc1409a04314c33288a4ea",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/40325e56dd0f339978ed015738b32a4d57e924fe"
        },
        "date": 1701385466239,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 6989.027691551131,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8634.429767311554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12908.818230114399,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17693.263172613228,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16692.673150695042,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687311.1750360583,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8629.829394994427,
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
          "id": "f1deb8ec78cd446bc6310b1528a5d71e1d42989e",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.18 (#6033)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-11-30T17:00:42-06:00",
          "tree_id": "6690246653b320ced2b594971628fb6e9562b51d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f1deb8ec78cd446bc6310b1528a5d71e1d42989e"
        },
        "date": 1701385718166,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7103.162031826074,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8527.756691978773,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12550.166013606311,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18827.258018671477,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15712.346962324507,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680901.8563282292,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8594.024300074116,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e447e347e143ebb1838b01c6bb23bd40e3c4f723",
          "message": "Allow for simpler creation of start-only and end-only SpanProcessors. (#5923)",
          "timestamp": "2023-12-04T11:40:09-06:00",
          "tree_id": "108d1bb4262932a5bea660011c03a5f37e004f94",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e447e347e143ebb1838b01c6bb23bd40e3c4f723"
        },
        "date": 1701717997804,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7182.743383388283,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8584.975880724782,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13143.06349842829,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17567.1668367022,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15844.457776047479,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691360.3112929799,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8569.84250688858,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3f1b9ed263f39f8538e9961943ccc0dd1d3f9964",
          "message": "Group spotless renovate updates (#6042)",
          "timestamp": "2023-12-04T13:40:56-06:00",
          "tree_id": "bc5aa54161a2642ae95400db3a53f8cce8c83539",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3f1b9ed263f39f8538e9961943ccc0dd1d3f9964"
        },
        "date": 1701723627867,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7062.394643604826,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8550.721871288826,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11592.161682390028,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16868.908249292792,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17291.514055033444,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687123.6213997386,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.457211817416,
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
          "id": "247ef4d26e511fc9aedaf3acca6e956c0358731e",
          "message": "Update spotless packages to v6.23.3 (#6043)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-04T15:01:15-06:00",
          "tree_id": "3af2461e9f38ec6a62853a9c20164f841133bb34",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/247ef4d26e511fc9aedaf3acca6e956c0358731e"
        },
        "date": 1701724304981,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7080.138982835333,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8246.924636472566,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11374.602730954677,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18269.244853398213,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15925.198819607263,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1673913.2177942726,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8498.063368398016,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9a3391d5e064437b87ddfc06492984d35dc1d1da",
          "message": "Serialize log body any value (#5938)",
          "timestamp": "2023-12-07T13:29:41-06:00",
          "tree_id": "211383bb7cbff6e1cde00d28983ac534137416b0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9a3391d5e064437b87ddfc06492984d35dc1d1da"
        },
        "date": 1701977921868,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7126.730460295487,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8623.919944933055,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12426.48414185704,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17161.007768264324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13878.231707954541,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686029.4194451268,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8630.56610883111,
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
          "id": "dc8b44f00bb639d264f51d41323d708c0ad554c9",
          "message": "Update actions/stale action to v9 (#6056)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:52:58-06:00",
          "tree_id": "d4646d2caee27a4acfd9caac73059e39f8e09a37",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/dc8b44f00bb639d264f51d41323d708c0ad554c9"
        },
        "date": 1701982600034,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7199.015150720481,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8311.039382219691,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13229.303380234089,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17254.47601035029,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16611.487360065752,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687178.7415199678,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.375947083865,
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
          "id": "07413b15a5955d05d1e462dcb043dc0d136162fc",
          "message": "Update dependency checkstyle to v10.12.6 (#6055)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:53:20-06:00",
          "tree_id": "92d12e1fd4c37960b70a44fb9462c2c14a533891",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/07413b15a5955d05d1e462dcb043dc0d136162fc"
        },
        "date": 1701982874861,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7073.17478266925,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8366.127434028425,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12726.858233095434,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16276.46936206052,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21543.731302928532,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684126.579165842,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.870869047923,
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
          "id": "cf3b3901d92ad74e0329ca7f6573c03f2e88f6d1",
          "message": "Update dependency com.toasttab.android:gummy-bears-api-21 to v0.8.0 (#6041)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:54:29-06:00",
          "tree_id": "ccf7b8617ffd8a85369e846dadd1741d510a0410",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cf3b3901d92ad74e0329ca7f6573c03f2e88f6d1"
        },
        "date": 1701983097688,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7119.26631269846,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8308.038696059322,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12788.697341535542,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18054.306132869962,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13781.562451790935,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691974.0832092792,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.622761827197,
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
          "id": "4e1771605bfd64699976987196c841996d674411",
          "message": "Update dependency io.grpc:grpc-bom to v1.60.0 (#6051)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:54:44-06:00",
          "tree_id": "ba2a850fb5698713ccbaaa8d3ef9537908b05efe",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4e1771605bfd64699976987196c841996d674411"
        },
        "date": 1701983332528,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7136.742237502926,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8665.275153235603,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13313.688721683535,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16471.79425746242,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15117.853099772805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684340.725247652,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8624.811146881677,
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
          "id": "d491facd83b3e938b83a6f79167db519fa1d3053",
          "message": "Update dependency com.tngtech.archunit:archunit-junit5 to v1.2.1 (#6038)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:54:59-06:00",
          "tree_id": "0ca5a5b29f2328db9245fd9c2388dee0f7e08ee1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d491facd83b3e938b83a6f79167db519fa1d3053"
        },
        "date": 1701983555459,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7074.360888634817,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8595.495258927924,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13444.554264627606,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17205.986198381164,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15172.358801206841,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691230.3816085965,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8623.17952632536,
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
          "id": "377809536acbf5c2c40cb8525c3a618e08e78297",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.3 (#6036)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T14:55:12-06:00",
          "tree_id": "b7f7a788c15d8feff5f0fd3f6dd3d0ae2ed20b4b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/377809536acbf5c2c40cb8525c3a618e08e78297"
        },
        "date": 1701983849920,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7205.359383521268,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8631.875967180418,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12789.495195476546,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17150.46624775041,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15431.96185146946,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675800.4963019197,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8594.95715378464,
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
          "id": "f36cc92b858236c4009fb3794c17c9f760ba6a0c",
          "message": "Update plugin com.gradle.enterprise to v3.16 (#6050)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-07T15:15:24-06:00",
          "tree_id": "9b93b73fc4be9169ba2fe0eb51f4f9951c283b63",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f36cc92b858236c4009fb3794c17c9f760ba6a0c"
        },
        "date": 1701984365386,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7229.646934027648,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8077.670742985867,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12951.18968400715,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17997.229167503483,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14795.6935356631,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677575.102913707,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8553.141787334434,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "Jonas.Kunz@gmx.net",
            "name": "Jonas Kunz",
            "username": "JonasKunz"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c8e1e3619e922981922a1034ce07e2a13e99d819",
          "message": "Improve autoconfiguration capabilities for adding, delaying or dropping spans (#5986)",
          "timestamp": "2023-12-07T15:41:03-06:00",
          "tree_id": "f18a93c2fabc703e210a2f3893699ec74093b9cd",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c8e1e3619e922981922a1034ce07e2a13e99d819"
        },
        "date": 1701986996997,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7106.590524363095,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8573.645935530181,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13388.197899272558,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18570.931505197943,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16354.71483485286,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689351.369323443,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.950175445336,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "gregor.zeitlinger@grafana.com",
            "name": "Gregor Zeitlinger",
            "username": "zeitlinger"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "2b33f9fb070822161206a8a62cfde04b214fc3e6",
          "message": "Extended tracer (#6017)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2023-12-07T16:27:57-06:00",
          "tree_id": "907fa3dd76f69b6c676c91f355022ba96e676519",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2b33f9fb070822161206a8a62cfde04b214fc3e6"
        },
        "date": 1701988311685,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7173.549536910338,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8512.661936312295,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13367.632741760168,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16648.428614092663,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15556.104597780844,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682583.6798335302,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8589.832803774263,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "902d68cf2abd081aea8de44898a38cc718e4bd56",
          "message": "Add connectTimeout configuration option OtlpHttp{Signal}Exporters (#5941)",
          "timestamp": "2023-12-07T16:28:15-06:00",
          "tree_id": "26bca9975adc535f6b1ba629a3a0bdf84022a332",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/902d68cf2abd081aea8de44898a38cc718e4bd56"
        },
        "date": 1701988544500,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7266.547893463797,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8561.74453164918,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12753.873245051689,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16996.3349966005,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14504.264260832313,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688455.3046799046,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.800555787777,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f1fc1c75b837c9b799e1826c58ef1c82b6c3d24c",
          "message": "Revert com.gradle.enterprise 3.16 upgrade (#6059)",
          "timestamp": "2023-12-07T17:22:21-06:00",
          "tree_id": "fd7247b39c3ab20285d3f247de0a8f7f04b625e4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f1fc1c75b837c9b799e1826c58ef1c82b6c3d24c"
        },
        "date": 1701991586694,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7058.130039462261,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8638.627319534246,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13405.944012573123,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16992.86250351833,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16436.555780143914,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686375.9178735327,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.827083236027,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "mr.mvagusta@gmail.com",
            "name": "Chung Nguyen",
            "username": "chungngoops"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3bb221fb5d73f28018d4f119bcaaa939fc675678",
          "message": "add hasAttributesSatisfying overload to AbstractPointAssert (#6048)",
          "timestamp": "2023-12-08T08:24:09-06:00",
          "tree_id": "a42bc46e95011c73e3ca11c31b8eee18026b158e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3bb221fb5d73f28018d4f119bcaaa939fc675678"
        },
        "date": 1702045680968,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7132.557042888453,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8341.489990441758,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13186.18441316125,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18482.8689699729,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13826.644719402615,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687212.486793782,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8591.14941609128,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "pfindeis@cisco.com",
            "name": "Peter Findeisen",
            "username": "PeterF778"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9f3456fd5d81df4efb293066ea5ef74ccb4cca1f",
          "message": "Issue 6037 - Wrapping \"invalid\" SpanContexts in Span does not preserve SpanContext (#6044)",
          "timestamp": "2023-12-08T08:24:36-06:00",
          "tree_id": "cec1722fe6dd6d07ce286dd6448e7bb8682dd503",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9f3456fd5d81df4efb293066ea5ef74ccb4cca1f"
        },
        "date": 1702045907794,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7144.060786788797,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8475.798506045905,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13218.688108885759,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16684.178700074324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 12693.490514260531,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690800.48538183,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8592.999693400317,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4c6039769a4756575a9d5fb6de16bfbd9ff5906f",
          "message": "Add OTLP header supplier configuration option (#6004)",
          "timestamp": "2023-12-08T08:37:14-06:00",
          "tree_id": "cd9b013b8ce83234441aee1b895a257eb50d475f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4c6039769a4756575a9d5fb6de16bfbd9ff5906f"
        },
        "date": 1702046466727,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7161.717352124719,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8600.495303176174,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13076.054519112655,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16036.864862025928,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14615.865337830011,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681811.974482571,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8630.272518287355,
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
          "id": "05db74f4f4272a39ca1e8a2de23c51ea3ef1e268",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.4 (#6064)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-08T08:40:20-06:00",
          "tree_id": "6142f5b590929cb428ee22f07d4b17bc1b336669",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/05db74f4f4272a39ca1e8a2de23c51ea3ef1e268"
        },
        "date": 1702046762677,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 6977.197372206457,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8232.580770379906,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10871.11072967628,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17131.998030498762,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15749.888617109256,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675984.8578382563,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8560.88860913106,
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
          "id": "e90a6f636b875cd271240f35d10449b34321aa9e",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.26.4 (#6062)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-08T09:15:35-06:00",
          "tree_id": "39bfb676f4c4a325339435d46286560c1881cb15",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e90a6f636b875cd271240f35d10449b34321aa9e"
        },
        "date": 1702048761551,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7141.836507163247,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8679.487055276728,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12049.053521175181,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16741.96284544079,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13663.517401323032,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690665.3677611533,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.518810040743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3a6613f9a35981f1172c38853f50d6f8ba8c398b",
          "message": "Prepare for 1.33.0 release (#6065)",
          "timestamp": "2023-12-08T13:57:04-06:00",
          "tree_id": "24a60f479989b66815815974c2fcda7e73cdffd9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3a6613f9a35981f1172c38853f50d6f8ba8c398b"
        },
        "date": 1702069407733,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7085.506981150925,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8457.171132127743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12896.694358746616,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16525.285927483976,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15108.563780782299,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687799.4126376691,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8576.694545554554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3086418d625cc25dddbd14d48615075ba446342f",
          "message": "Update version to 1.34.0 (#6067)",
          "timestamp": "2023-12-08T14:21:43-06:00",
          "tree_id": "c565057518a06bc0dc1759afd5ed826550049daa",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3086418d625cc25dddbd14d48615075ba446342f"
        },
        "date": 1702070095125,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7155.891891211242,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8361.490964264709,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13270.45446948892,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18295.70874622086,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16462.578676400506,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682132.8081522738,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8599.485798816755,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6e536238df5989fb100b198d91fdf733fa5ae5f6",
          "message": "Post release 1.33.0 (#6068)",
          "timestamp": "2023-12-08T21:50:39-06:00",
          "tree_id": "eff859903feea40cd2d75fdc638e745fa81be8d1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6e536238df5989fb100b198d91fdf733fa5ae5f6"
        },
        "date": 1702094061132,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7207.567097385478,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8706.112215023659,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12952.027197434747,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17838.245854557645,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16679.679087910128,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690215.8477748309,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8637.38204756057,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ffd53c7d56f3d14cdb13e8489d18d3d14b71da33",
          "message": "Memory Mode: Adding first part support for synchronous instruments - storage (#5998)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2023-12-14T16:21:39-06:00",
          "tree_id": "fc498f7c89f606d98c43c5c827cd736f1ba7026b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ffd53c7d56f3d14cdb13e8489d18d3d14b71da33"
        },
        "date": 1702612447160,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7124.680837189798,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8345.817420153242,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10944.101562330921,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16578.570435013222,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16786.420370083437,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687906.1283881143,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8640.384492823589,
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
          "id": "5412905f212496edfafc4d8bec106af0b5021d82",
          "message": "Update dependency io.zipkin.brave:brave-bom to v5.17.0 (#6081)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:02:23-06:00",
          "tree_id": "766edb24b7ca25717a0e4b318f0c660e11319c18",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5412905f212496edfafc4d8bec106af0b5021d82"
        },
        "date": 1702945514772,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7071.835928161574,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8618.072163163,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10760.771547792008,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19138.499153649496,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14903.79190214295,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687155.3170602345,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8646.115954778053,
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
          "id": "97386403ce8d6671b05ec3d6f2559b87dec736be",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.7 (#6073)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:02:52-06:00",
          "tree_id": "b2cdad946de785053f379aeadbe8f1ca2c1833f2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/97386403ce8d6671b05ec3d6f2559b87dec736be"
        },
        "date": 1702945806569,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7043.266566209397,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8481.478664939357,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13092.84657063535,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17071.062548785387,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21292.122426318263,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678030.8749496788,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8583.066940349057,
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
          "id": "6c6800a8213664beb3da67e4440b540e94ff85bf",
          "message": "Update actions/upload-artifact action to v4 (#6077)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:03:44-06:00",
          "tree_id": "c28191f94cb274c8adab0e20344c24d3d30edc4c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6c6800a8213664beb3da67e4440b540e94ff85bf"
        },
        "date": 1702946023866,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7120.886975682962,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8565.242815590933,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13370.287034422852,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18511.098103477394,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15433.182265160494,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690906.4010790798,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.172755187135,
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
          "id": "ecd11ea5db6c99546383a3e4ee2a68c9c3937cdd",
          "message": "Update github/codeql-action action to v3 (#6074)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:04:20-06:00",
          "tree_id": "1b80aa77dcccf4e21167924d7173a9543ed02382",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ecd11ea5db6c99546383a3e4ee2a68c9c3937cdd"
        },
        "date": 1702946242127,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7224.53591076128,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8463.87437688556,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13533.797759030896,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18830.108075264176,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17064.553168880462,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690132.5366220907,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8609.10127819458,
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
          "id": "b3e6045a720997b6e90d15ad65879f768bb5272d",
          "message": "Update dependency io.netty:netty-bom to v4.1.104.Final (#6072)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:25:14-06:00",
          "tree_id": "94aaeb4adcf91fd4cb73c9c9d99b65b9aeae8f63",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b3e6045a720997b6e90d15ad65879f768bb5272d"
        },
        "date": 1702948183774,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7143.280544942824,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8524.337615654484,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12946.768730624448,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16098.031256218743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15238.712078289589,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683385.6387788379,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.784037984351,
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
          "id": "2634bcad7a1c4e878feb77dbf07d87647e7a538a",
          "message": "Update dependency io.zipkin.reporter2:zipkin-reporter-bom to v2.17.1 (#6070)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:25:23-06:00",
          "tree_id": "bd2c7353fcc8414233d078b139870cf98e23c0f2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2634bcad7a1c4e878feb77dbf07d87647e7a538a"
        },
        "date": 1702948404926,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7095.792274409555,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8333.713600719786,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13294.086756020431,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17469.94584058533,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16678.62932821207,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688878.6147254356,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8604.53529431989,
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
          "id": "867d25be415b1acaf0d5a0b9f16fb4016e09130e",
          "message": "Update dependency com.squareup.okio:okio-bom to v3.7.0 (#6080)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T11:25:34-06:00",
          "tree_id": "939a3ee06040d829c843782cf2df5f0163b57375",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/867d25be415b1acaf0d5a0b9f16fb4016e09130e"
        },
        "date": 1702948630065,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7142.129723629716,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8120.833971664555,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13550.799765048574,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18074.295479608583,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14599.772259125888,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688773.5378402031,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8617.94253105846,
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
          "id": "d1e2d09fa44d616e23c1802fa5f707cc96fa4bda",
          "message": "Update plugin com.gradle.enterprise to v3.16.1 (#6060)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T16:09:56-06:00",
          "tree_id": "d9f53523bf685b65f2537cdaeb7756f0135e672d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d1e2d09fa44d616e23c1802fa5f707cc96fa4bda"
        },
        "date": 1702950905354,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7071.3645587707315,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8616.811292480132,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13049.005086640671,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17606.76018417586,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13601.348156562148,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682472.1626876376,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8570.99144100391,
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
          "id": "0b32fe2fdafeb08c6d0639c67bbe3ef6d0322ba8",
          "message": "Update dependency com.google.guava:guava-bom to v33 (#6083)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-18T16:57:42-06:00",
          "tree_id": "acab701056826d8bd30d64d4757b4541e0b8da43",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0b32fe2fdafeb08c6d0639c67bbe3ef6d0322ba8"
        },
        "date": 1702975169287,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7262.990830280029,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8486.85486433699,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13034.870662828282,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17248.45558051826,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13603.813227592163,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683989.4033913643,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.881077601152,
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
          "id": "eea1fe0e996cfdfe1e7ed373bb9a41c19bb90ade",
          "message": "Update dependency com.google.guava:guava to v33 (#6085)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-20T08:30:03-06:00",
          "tree_id": "88d46d492070524f13aa91eae757ae730addbfda",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/eea1fe0e996cfdfe1e7ed373bb9a41c19bb90ade"
        },
        "date": 1703149191271,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7134.527192518351,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8540.233200003137,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12867.235694459669,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19075.116385001344,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16221.803178090031,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675484.3532869325,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8564.762427843316,
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
          "id": "227580ba0956fcd43429d684b7e0798c8723481a",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.5 (#6094)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2023-12-22T15:53:29-08:00",
          "tree_id": "b8ace30fcc22ec5f280cbf81129fd79cddede72b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/227580ba0956fcd43429d684b7e0798c8723481a"
        },
        "date": 1703289435288,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7052.602377912756,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8478.8190185296,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13324.786910216711,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18954.484365726108,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16674.805991109715,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690366.8658556393,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.479500282472,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "jean.bisutti@gmail.com",
            "name": "Jean Bisutti",
            "username": "jeanbisutti"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4f6cc700b58d88e1bbe5b4fd65e82c11dec7976d",
          "message": "Ability to access version.properties API file with GraalVM native (#6095)",
          "timestamp": "2024-01-02T10:03:51-06:00",
          "tree_id": "be02e0b3cad63dd58ab1e5e17331fd00c028b9fe",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4f6cc700b58d88e1bbe5b4fd65e82c11dec7976d"
        },
        "date": 1704211649959,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7229.73208231294,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8175.570028078995,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12607.42558451544,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15719.569527780823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14513.824881942239,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690580.802391234,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8604.034426212045,
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
          "id": "e588eb80420a29a020b41618169546e5198d60aa",
          "message": "Update dependency org.assertj:assertj-bom to v3.25.0 (#6102)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:17:11-06:00",
          "tree_id": "8c0a1a363534bb794424adf8c99f7f7c7127fb80",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e588eb80420a29a020b41618169546e5198d60aa"
        },
        "date": 1704216057138,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7180.202873226276,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8293.277871915489,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12626.64040460696,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17348.739989901514,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14934.24473676411,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684899.5462546013,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8587.140211644863,
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
          "id": "1b4f5aeab5f9d788c6af68fa8b4b0fb1ee66626d",
          "message": "Update dependency checkstyle to v10.12.7 (#6101)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:34:11-06:00",
          "tree_id": "1ac6abcb0cff5722a872f31d1f2d697399af8c0d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1b4f5aeab5f9d788c6af68fa8b4b0fb1ee66626d"
        },
        "date": 1704217125086,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7112.454693993496,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8269.152489317143,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12030.398364581644,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17049.967571575027,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15019.290120919288,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683655.460532606,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8578.383672361651,
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
          "id": "fcd7812d776989a38425866f426e9608aed77e22",
          "message": "Update slf4jVersion to v2.0.10 (#6099)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:36:58-06:00",
          "tree_id": "cf8310800fd5c6b9bea0b2eff2d621371da9e7ec",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fcd7812d776989a38425866f426e9608aed77e22"
        },
        "date": 1704217348126,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7179.492814538256,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8229.522626915637,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13352.084577448957,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17266.12951839662,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16925.187940425007,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685527.871143201,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8576.216972542805,
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
          "id": "a265b288fd569f8e96bbbadabc4de3e17c80bea2",
          "message": "Update dependency com.fasterxml.jackson:jackson-bom to v2.16.1 (#6096)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:37:19-06:00",
          "tree_id": "3dc55cdb5709fff13d47692a05aecb26a5ae5678",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a265b288fd569f8e96bbbadabc4de3e17c80bea2"
        },
        "date": 1704217578745,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7189.07255970345,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8319.058892681473,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13010.408067746499,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18396.517347493376,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16114.999068964587,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678001.2619851169,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8624.091812987048,
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
          "id": "d81dabff4b96c46ef7239ffded1ccde736f63a70",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.19 (#6097)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:37:39-06:00",
          "tree_id": "6e4509a0ae95ca8f89bceeb68c9cfe28b85b44fd",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d81dabff4b96c46ef7239ffded1ccde736f63a70"
        },
        "date": 1704217832515,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7156.407915911085,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8378.994454558657,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13310.658790017435,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16885.467099946043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15858.732699655837,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678635.2786361817,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8604.532423292489,
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
          "id": "b48893bb9daf4a9c9546ef8250bd84fafd764b9e",
          "message": "Update dependency io.grpc:grpc-bom to v1.60.1 (#6091)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:43:10-06:00",
          "tree_id": "4f3693799c9b1770ced8b51902cb0da2cfdd6f66",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b48893bb9daf4a9c9546ef8250bd84fafd764b9e"
        },
        "date": 1704218061920,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7208.7063278117375,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8480.982188314812,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12166.440263109635,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17864.826469941087,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21280.05228546517,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682728.2963133429,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8579.110064130005,
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
          "id": "0c436e0a297a8dc5e5c5d79f824e46841a523ca6",
          "message": "Update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v1.9.22 (#6090)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-02T11:54:34-06:00",
          "tree_id": "828ba534ea23b490fc8adf6d792c1b2275533d2a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0c436e0a297a8dc5e5c5d79f824e46841a523ca6"
        },
        "date": 1704218370355,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7132.36461876604,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8271.470954868313,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13042.89806896821,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18571.790648268438,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13917.679288829953,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684908.246903763,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8555.323556876292,
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
          "id": "e1b1963088826459757ce05dfd759d824af2003b",
          "message": "Update errorProneVersion to v2.24.0 (#6092)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-01-02T12:59:29-06:00",
          "tree_id": "a9afeee5821e1ffa949d6d280bcbc4026c45cfa1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e1b1963088826459757ce05dfd759d824af2003b"
        },
        "date": 1704222222868,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7244.965370088055,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8421.012810178096,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13136.29308486823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18070.99895326724,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14640.659838352241,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678594.0718915197,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8586.687083435972,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "60ae0af7f683af11588bc540c052d8f3bc40ef74",
          "message": "Add compressor SPI to support additional compression algos (#5990)",
          "timestamp": "2024-01-02T15:00:54-06:00",
          "tree_id": "f32771c79655d75637fb8727fbff013f6771f4f3",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/60ae0af7f683af11588bc540c052d8f3bc40ef74"
        },
        "date": 1704241405234,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7078.954511052208,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8409.633149212266,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11791.737730508317,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17705.384680330033,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14033.535840305773,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690027.873089249,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8469.428799076046,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "42679988e2621c5c68fbdc50bec6dab86a790877",
          "message": "Fix owasp dependency check (#6086)",
          "timestamp": "2024-01-02T17:33:13-06:00",
          "tree_id": "319594d7ddad771de720e27d6fe20b5efc1f004e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/42679988e2621c5c68fbdc50bec6dab86a790877"
        },
        "date": 1704241694638,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7139.328479257051,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8532.588848630452,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13672.160372292163,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17263.78954904522,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15431.829671257501,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685257.3321141428,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8589.034786299191,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "de65a4ba1ef8d1aa55213c658c316fa369f39e30",
          "message": "Test OTLP exporters with different OkHttp versions (#6045)",
          "timestamp": "2024-01-02T17:53:08-06:00",
          "tree_id": "4d3adb58e1f0531c7e8c4df5776d35d0ef8561d1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/de65a4ba1ef8d1aa55213c658c316fa369f39e30"
        },
        "date": 1704241916220,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7128.791436761248,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8334.934043539188,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13424.946542487916,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 13207.251484236234,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14929.67271594077,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691243.2075313486,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8630.289976426782,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "fabian@fstab.de",
            "name": "Fabian Stäber",
            "username": "fstab"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d45fb3f5dc458b80bbab2ebfb91caa14dcb392e1",
          "message": "Exponential Histogram support to the Prometheus exporter (#6015)\n\nSigned-off-by: Fabian Stäber <fabian@fstab.de>",
          "timestamp": "2024-01-03T09:51:35-06:00",
          "tree_id": "f3c36c863159ec26d44bea6b8226f14d8786c198",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d45fb3f5dc458b80bbab2ebfb91caa14dcb392e1"
        },
        "date": 1704297319097,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7218.9545363118,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8517.159336829833,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12987.95655381616,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18548.444426659633,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16204.716042842072,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1692306.530079721,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.1237618737,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "17b8f4355da0932e25a66fffd03f01ad8ea95f5e",
          "message": "Read normalized otel.config.file property (#6105)\n\nCo-authored-by: Trask Stalnaker <trask.stalnaker@gmail.com>",
          "timestamp": "2024-01-03T11:50:38-06:00",
          "tree_id": "e99a5b491f801de04fd4f8111180173c52f43877",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/17b8f4355da0932e25a66fffd03f01ad8ea95f5e"
        },
        "date": 1704307099936,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7054.205863142561,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8258.469961086106,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13939.044016549655,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18330.62027240234,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21399.17063669898,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691630.021705695,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8634.495551378353,
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
          "id": "517ef88ca16928d6d514125c78c166e3e663dc2f",
          "message": "Update errorProneVersion to v2.24.1 (#6111)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-03T12:16:00-06:00",
          "tree_id": "29fa667bef2aa0ca29b2f6e053712666394512cc",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/517ef88ca16928d6d514125c78c166e3e663dc2f"
        },
        "date": 1704307353348,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7109.56765902493,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8296.371186355214,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13236.037841158273,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17459.832046721374,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17730.09847022422,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681412.350626442,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8617.53870443061,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bc0df3b4d587350134c20d196b6ecae78079837b",
          "message": "Fix autoconfigure javadoc explaining behavior when disabled (#6109)",
          "timestamp": "2024-01-03T15:29:26-06:00",
          "tree_id": "6e83f81dadc72a8f2da2ad4d9023335eae7a785a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bc0df3b4d587350134c20d196b6ecae78079837b"
        },
        "date": 1704317582414,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7269.806004757976,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8463.943442514532,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11568.149148731816,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16940.579919524702,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13987.858538207798,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687886.7980315213,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8631.177260322718,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "63fe7084d28388902fc6781d4c45ef942c0c224e",
          "message": "Only call SpanProcessor onStart / onEnd if required (#6112)",
          "timestamp": "2024-01-03T15:32:08-06:00",
          "tree_id": "e6e3be10efde01f716eda56ebcb4d1a567afc610",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/63fe7084d28388902fc6781d4c45ef942c0c224e"
        },
        "date": 1704317803740,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7195.676382333908,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8359.709994053192,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13699.0772477514,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16925.883825908986,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16391.86024889667,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685849.958953722,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8573.82750081243,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b4ed53211bb6f893bed9dbde119847d180638862",
          "message": "Propagate serialization IOException instead of rethrowing as runtime (#6082)\n\nCo-authored-by: Ricardo Mestre <ricardom57@hotmail.com>",
          "timestamp": "2024-01-03T17:06:47-06:00",
          "tree_id": "81fc759301c780c41591cf0e8a97f46c0810d84e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b4ed53211bb6f893bed9dbde119847d180638862"
        },
        "date": 1704325766220,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7148.242666946478,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8434.453954069028,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12800.986141126541,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16646.767376126965,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14531.397075933924,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681564.4091196384,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8606.338233405368,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "da7796b3b53a550a632bf206cc3da0d42c05b680",
          "message": "Use minimal fallback managed channel when none is specified (#6110)",
          "timestamp": "2024-01-04T09:36:03-06:00",
          "tree_id": "0f4e4c6a331142052def0f07ba5e74700a246549",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/da7796b3b53a550a632bf206cc3da0d42c05b680"
        },
        "date": 1704384243119,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7196.000877550282,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8483.0984338531,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13705.622699455358,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 14634.433438402028,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16753.344588038362,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685643.1716936554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8593.008045994795,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "5a0dd7ef25039e1cf37c6a66fe19d6751c74bfe2",
          "message": "Base2ExponentialHistogramAggregation maxBuckets must be >= 2 (#6093)",
          "timestamp": "2024-01-04T11:43:23-06:00",
          "tree_id": "c989a2e68789b54ec5194957994086bcdec9ac4c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5a0dd7ef25039e1cf37c6a66fe19d6751c74bfe2"
        },
        "date": 1704392844285,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7103.447785709496,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8312.024159354612,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11841.786256814656,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17117.67385528281,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16532.203689135647,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689367.5362644505,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8627.516241706078,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "3449c58ef588a2bec990d449bb378dea310eed68",
          "message": "Convert histogram measurements to double before passing recording exemplar reservoir (#6024)\n\nCo-authored-by: Trask Stalnaker <trask.stalnaker@gmail.com>",
          "timestamp": "2024-01-04T11:43:42-06:00",
          "tree_id": "81044f29f802f7831b4319e70d556206497f8ea7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3449c58ef588a2bec990d449bb378dea310eed68"
        },
        "date": 1704393082957,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7125.799718132396,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8281.081117819022,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13807.418057081231,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15358.254301882504,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16524.525861864036,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681456.0443249536,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.04312155068,
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
          "id": "f4b5bbe8293c0631c9b9de5540f6550dab6c9491",
          "message": "Update dependency org.assertj:assertj-bom to v3.25.1 (#6106)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-04T13:01:04-06:00",
          "tree_id": "e01093db8b79528ffb757d7bc9a3cf52b2b486b6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f4b5bbe8293c0631c9b9de5540f6550dab6c9491"
        },
        "date": 1704398064760,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7066.358762576889,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8366.148271441125,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13382.054811688384,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17751.53459596922,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14659.310691224184,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684450.0553071382,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.812570886854,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "HaloFour@users.noreply.github.com",
            "name": "HaloFour",
            "username": "HaloFour"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "07351a2e9f20ba4e47fd60b71015966263da384d",
          "message": "Add option to export unsampled spans from span processors (#6057)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-04T14:33:30-06:00",
          "tree_id": "5c8e64d8690fae2ab818bf9acb3e69a6913b8d4b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/07351a2e9f20ba4e47fd60b71015966263da384d"
        },
        "date": 1704408327765,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7145.722692648518,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8645.009219092975,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13395.706528247978,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18334.776841620274,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15432.027009306112,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681733.9920280967,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.14174193655,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1f2a8fef1745fd7be0e6c0e16317f17f7930c56d",
          "message": "Prepare 1.34.0 (#6115)",
          "timestamp": "2024-01-05T09:11:06-06:00",
          "tree_id": "88d83422b912c7875daa1e292cd8d6dc4c09a858",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1f2a8fef1745fd7be0e6c0e16317f17f7930c56d"
        },
        "date": 1704467687956,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7194.583970313921,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8580.098897752876,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13976.988777101296,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16716.73717929681,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14339.599955279811,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690924.4892760082,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8576.57628556946,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c9ea2381215483189a93f5207a940a29638cbb22",
          "message": "Update version to 1.35.0 (#6116)",
          "timestamp": "2024-01-05T09:39:00-06:00",
          "tree_id": "0cea2c2cad891484d2caa32f932b0fea0e48e3e0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c9ea2381215483189a93f5207a940a29638cbb22"
        },
        "date": 1704471890147,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7115.835151218633,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8647.274095346938,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12693.405319236637,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18144.884307244887,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14416.453077097362,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1679408.3547781135,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8594.250868105175,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d9f9812d4375a4229caff43bd681c50b7a45776a",
          "message": "Post release 1.34.0 (#6118)",
          "timestamp": "2024-01-05T11:19:28-06:00",
          "tree_id": "3dcfccdcce0197343e699a8b73a6c5c925f93741",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d9f9812d4375a4229caff43bd681c50b7a45776a"
        },
        "date": 1704475384765,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7153.653510667356,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8387.634433704148,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10657.089231747543,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16871.40995657404,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14492.4400426715,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686920.66605548,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.81702045476,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ff823f4346f62f316134cb0ff145eedd544e83d2",
          "message": "Recreate graal RetryPolicy issue (#6139)",
          "timestamp": "2024-01-11T15:52:44-06:00",
          "tree_id": "a2b60b4a337e613d735b6b5a90f03871d25473c6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ff823f4346f62f316134cb0ff145eedd544e83d2"
        },
        "date": 1705019260144,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7187.436051494381,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8284.177595548736,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13371.630189160602,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21139.730558610536,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16511.382613313188,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691503.2054346304,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8641.17191529688,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "38b24890afcb0f9b8261b32940d727733e1c208f",
          "message": "Fix graal (#6134)",
          "timestamp": "2024-01-11T15:52:56-06:00",
          "tree_id": "a2b60b4a337e613d735b6b5a90f03871d25473c6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/38b24890afcb0f9b8261b32940d727733e1c208f"
        },
        "date": 1705019478558,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7177.799146367429,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8435.122124586183,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13601.306279184928,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18687.920329874007,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16465.382277117886,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687135.8271703701,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8645.778207816582,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c1490728c92812b4c047d9171734a86a03f8dd96",
          "message": "Restore prometheus metric name mapper tests, fix regressions (#6138)",
          "timestamp": "2024-01-11T15:53:10-06:00",
          "tree_id": "4fd8a0e4fb031b49bc37b1b01e9888e03604cbc1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c1490728c92812b4c047d9171734a86a03f8dd96"
        },
        "date": 1705019696307,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7266.963630665469,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8333.419148765845,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13550.056984075676,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17559.79175190503,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13810.656472720435,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688616.242619624,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.38308980539,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "9c3af76ab90b42da1eea0070413b0da54e88a408",
          "message": "Merge change log updates from release/v1.34.x (#6145)",
          "timestamp": "2024-01-11T17:44:44-06:00",
          "tree_id": "2bd6f53fe64414d253c91de7ca4baefa261a3780",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9c3af76ab90b42da1eea0070413b0da54e88a408"
        },
        "date": 1705020167550,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7239.806567653136,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8619.555781329236,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11998.393812994374,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18389.563728363173,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15458.390324387723,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688558.4608877476,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.983535537942,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "30c75b471512048d91035be213094582bed6e6fd",
          "message": "Delete jaeger exporters (#6119)",
          "timestamp": "2024-01-12T16:52:32-06:00",
          "tree_id": "b1e67043c9fe3712c945d800ef13adaeb799a51b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/30c75b471512048d91035be213094582bed6e6fd"
        },
        "date": 1705104193928,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7140.058940209809,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8537.2032319988,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13693.373634291598,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17120.748736979673,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14489.070479046732,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688797.8918697424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8539.81747693161,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0641844059290aea2e2eeb4bf87aa74c98703337",
          "message": "Memory Mode: Adding 2nd part support for synchronous instruments - exponential histogram (#6058)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-12T16:52:54-06:00",
          "tree_id": "4fb4d239a0fba193452ba2a7201e163c7e8921d7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0641844059290aea2e2eeb4bf87aa74c98703337"
        },
        "date": 1705104436718,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7201.67513776729,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8425.72075553014,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13527.458092164545,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18455.886746669963,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16335.032690803137,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687098.7661565798,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8597.88669589016,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "64215+codefromthecrypt@users.noreply.github.com",
            "name": "Crypt Keeper",
            "username": "codefromthecrypt"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "208118af1841fa6019300f086510009584f4fea9",
          "message": "Update dependency io.zipkin.reporter2:zipkin-reporter-bom to 3.1.1 (#6129)\n\nSigned-off-by: Adrian Cole <adrian@tetrate.io>",
          "timestamp": "2024-01-12T17:02:11-06:00",
          "tree_id": "cde129c5e53042da9de70cb567ffcdde05d6b4dc",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/208118af1841fa6019300f086510009584f4fea9"
        },
        "date": 1705104656909,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7259.7984657128245,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8672.105581633807,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12316.854704067575,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17366.506110441253,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15094.52396578298,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690624.9752290784,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8629.945133958527,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "dbaba2fd9168bb413d233942e533fada2b2c761e",
          "message": "Post release 1.34.1 (#6146)",
          "timestamp": "2024-01-12T17:15:46-06:00",
          "tree_id": "658e26f2f46538a682940460b5e4bc8c7022c0c4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/dbaba2fd9168bb413d233942e533fada2b2c761e"
        },
        "date": 1705104873472,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7152.752765377358,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8944.82327813116,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12916.472886788131,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21543.65752106392,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13931.817688456187,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688033.532605596,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8584.077441533682,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "64215+codefromthecrypt@users.noreply.github.com",
            "name": "Crypt Keeper",
            "username": "codefromthecrypt"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "491de708e19dfe3b1957747e7d93de97fda520fe",
          "message": "fixes link drift in sonatype (#6152)\n\nSigned-off-by: Adrian Cole <adrian@tetrate.io>",
          "timestamp": "2024-01-16T15:22:24-06:00",
          "tree_id": "ef9afddeed1c8d8411175a56fd9286aa46fd8532",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/491de708e19dfe3b1957747e7d93de97fda520fe"
        },
        "date": 1705449722979,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7275.137933471688,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8509.315930813496,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12099.126667976114,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15362.661791065166,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16481.35113894204,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689300.9586728823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8578.296209341966,
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
          "id": "6a79e6f32caf7a73e3456a623c3e7fb2bcc2f776",
          "message": "Update slf4j monorepo to v2.0.11 (#6155)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:47:17-06:00",
          "tree_id": "6735e36fba22df239077fcfc864225e34164b89d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6a79e6f32caf7a73e3456a623c3e7fb2bcc2f776"
        },
        "date": 1705539121901,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7219.995358761159,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8395.05583573342,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13388.683299077511,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16827.4146076975,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14503.10962857873,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690047.8949328854,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8599.672773659704,
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
          "id": "870a6bf25691fad134cd52d2fd84867a6535d765",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.9 (#6126)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:47:37-06:00",
          "tree_id": "7a2440a7cf29e8f103f6775ac06475375270c3cb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/870a6bf25691fad134cd52d2fd84867a6535d765"
        },
        "date": 1705539412056,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7277.3445998860325,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8350.408936705518,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12551.490253955275,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18335.53456803658,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15797.904066433422,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1679951.1204859565,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8560.783559551304,
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
          "id": "9faf11b605ac986742741f117a78913711224fe8",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.30.0 (#6122)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:47:58-06:00",
          "tree_id": "255338e91aea52e01269a2eed149c0068a8d2135",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9faf11b605ac986742741f117a78913711224fe8"
        },
        "date": 1705539636054,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7220.361493859384,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8714.227660984136,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12956.013745428714,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16157.066308610923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16636.19652428699,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690442.5471131324,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8629.746228546315,
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
          "id": "c5ca491dd6fe1807e548123506cc76e59d1d09fc",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.21 (#6150)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:52:47-06:00",
          "tree_id": "5dcad6b032956e61726e86f53cdf3788b2a45ea4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c5ca491dd6fe1807e548123506cc76e59d1d09fc"
        },
        "date": 1705539888923,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7174.461525021311,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8656.55863363056,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12283.188850268483,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17002.285873160614,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13801.588793337618,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677964.9083826754,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8563.212216303002,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "64215+codefromthecrypt@users.noreply.github.com",
            "name": "Crypt Keeper",
            "username": "codefromthecrypt"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f0d6c82337fb0979ecec7ced1bbee37745e97415",
          "message": "Update dependency io.zipkin.brave:brave-bom to 6.0.0 (#6131)\n\nSigned-off-by: Adrian Cole <adrian@tetrate.io>\r\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:54:42-06:00",
          "tree_id": "40625c8aee0493bbbc444781e443108b99d711d4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f0d6c82337fb0979ecec7ced1bbee37745e97415"
        },
        "date": 1705540362677,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7130.137295957966,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8605.529599081758,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13506.549778860805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18248.09891633304,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15085.99253075791,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688720.8554558798,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8618.318974369817,
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
          "id": "496965e1b4d0a22a5148efcca41cbbb7f18051e5",
          "message": "Update dependency com.google.protobuf:protobuf-bom to v3.25.2 (#6137)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T15:56:06-06:00",
          "tree_id": "43193b6a75d3b903cb7186b26afacebfb01cb2da",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/496965e1b4d0a22a5148efcca41cbbb7f18051e5"
        },
        "date": 1705540615599,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 6999.162440978672,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8488.568242529516,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13232.831085757227,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18519.90446330636,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14796.6789175405,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678445.4614874725,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8584.617776845083,
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
          "id": "2f69bec4288308cd2d0cff541faa2b9566db39ca",
          "message": "Update spotless packages to v6.24.0 (#6162)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T16:14:18-06:00",
          "tree_id": "33b0407eaceb99f81a07f96c1501dc2e32c124ef",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2f69bec4288308cd2d0cff541faa2b9566db39ca"
        },
        "date": 1705540910406,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7076.406434976157,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8704.050878201191,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13482.45511029098,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18341.734751961005,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16132.238200862925,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684907.872661199,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8588.38161156023,
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
          "id": "e337bd732ef32aed38bd13c587b3a28cece46287",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.6 (#6133)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T16:54:40-06:00",
          "tree_id": "56b3fb20d06b69e34f8b20e6d565ad5727ba8624",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e337bd732ef32aed38bd13c587b3a28cece46287"
        },
        "date": 1705544978959,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7233.576467928689,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8312.635260377128,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13175.759186565101,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19445.75291467978,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16756.228997341273,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690383.728766262,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8623.902407422813,
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
          "id": "4407ee72d626449dba7baecc86ef8c444322a0c6",
          "message": "Update dependency io.netty:netty-bom to v4.1.105.Final (#6154)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-17T16:54:58-06:00",
          "tree_id": "fcfb65aa7b14c99c3a69b650d93b5adb31356318",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4407ee72d626449dba7baecc86ef8c444322a0c6"
        },
        "date": 1705545204948,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7204.057892058996,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8296.376081571358,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12776.284560296626,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15834.837828276028,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 21478.752462673623,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684666.2841363177,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8654.561324238073,
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
          "id": "16d6e0e00abf12d43e310bccac4aba402fc49c8a",
          "message": "Update dependency io.grpc:grpc-bom to v1.61.0 (#6161)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-18T15:38:42-06:00",
          "tree_id": "82335f81de6f80ce79ade9b3a9cd123e5ff2aa33",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/16d6e0e00abf12d43e310bccac4aba402fc49c8a"
        },
        "date": 1705632028579,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7199.036405237293,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8407.269727366709,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13267.50630371043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15457.034143831857,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16030.116075624992,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688198.802342012,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.58028997203,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ede6a81a17298938ac8d7b4d836c17a7f4dec268",
          "message": "Stop including old artifacts in bom (#6157)",
          "timestamp": "2024-01-18T16:06:08-06:00",
          "tree_id": "c5a77ff79a448f33afbb4eca2d0341a876858b35",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ede6a81a17298938ac8d7b4d836c17a7f4dec268"
        },
        "date": 1705632317873,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7085.526781069126,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8406.493579485043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13124.180181608903,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15266.01407732722,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14374.26871490102,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675655.4810133218,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8581.266852663995,
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
          "id": "e4b31cea3de13db7262540457e598282f56d9978",
          "message": "Update dependency io.netty:netty-bom to v4.1.106.Final (#6166)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-19T11:00:10-06:00",
          "tree_id": "f68038daadb5d5710797d04b4af7d6a26b47b14a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e4b31cea3de13db7262540457e598282f56d9978"
        },
        "date": 1705683831403,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7254.765778296404,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8452.88777460668,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12789.00115551829,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18394.4224738962,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14786.388150956142,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691743.6310033542,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8641.287653606145,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ee6c9867d736af7359003e0b2f61ebf082e6fe3e",
          "message": "Align file configuration with latest changes to spec (#6088)",
          "timestamp": "2024-01-23T16:57:45-06:00",
          "tree_id": "98e1cbb5d56492b2689b3bd32100a3c9881914db",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ee6c9867d736af7359003e0b2f61ebf082e6fe3e"
        },
        "date": 1706054902106,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7150.975721756597,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8514.309495612095,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13415.85393660928,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18383.257483674926,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16954.144568583364,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689153.3103597541,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8525.184699617776,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "64215+codefromthecrypt@users.noreply.github.com",
            "name": "Crypt Keeper",
            "username": "codefromthecrypt"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0e4986a0736b799038c80a5c856fc320fa028001",
          "message": "Update dependency io.zipkin.reporter2:zipkin-reporter-bom to v3.2.1 (#6151)\n\nSigned-off-by: Adrian Cole <adrian@tetrate.io>\r\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-24T16:52:57-06:00",
          "tree_id": "4823803d3a3dd5124a176723a6ae726ebf60f948",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0e4986a0736b799038c80a5c856fc320fa028001"
        },
        "date": 1706137101455,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7100.829613823612,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8865.923183949944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13530.990877231397,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17026.65947431237,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16337.094332134428,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687060.357934329,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.831495225615,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "737dfef4e62225158017c0767f0b4ffc25b7f1b6",
          "message": "Memory Mode: Adding 3rd and last part support for synchronous instruments - exponential histogram (#6136)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-25T09:23:59-06:00",
          "tree_id": "9ff8ead55f83726fa54f3243b624222403c8d65d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/737dfef4e62225158017c0767f0b4ffc25b7f1b6"
        },
        "date": 1706196625984,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7256.971831244694,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8492.814382690802,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13199.955512240142,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16736.257846418397,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16020.280413744176,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690081.3274207762,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8610.473320869736,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8d1cad2ae13200e4fffbefc6e8e38d8e40479657",
          "message": "Memory mode: Adding support for synchronous instruments - explicit histogram (#6153)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-01-25T12:18:28-06:00",
          "tree_id": "815f04da159fe27b67fc77f52de96d94f17ab6c5",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8d1cad2ae13200e4fffbefc6e8e38d8e40479657"
        },
        "date": 1706207297089,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7192.500728987101,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8441.076799405966,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11848.709811553865,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16600.66411701038,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13810.235944080752,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685250.3692871642,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8600.203140617446,
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
          "id": "c8a968a99b1f8a3dc71ce0be17b9749a3c9176ec",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.4 (#6174)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-25T17:36:09-06:00",
          "tree_id": "f78bcf840d7ad8d72727c781ce9d373c66abcd76",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c8a968a99b1f8a3dc71ce0be17b9749a3c9176ec"
        },
        "date": 1706230440332,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7177.520502411249,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8437.564321430953,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13184.282304223578,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17680.97986681552,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15283.110863545993,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689559.2046341244,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.15959742781,
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
          "id": "91a5dd9e8a347455423b9cc672d4653e95792b2b",
          "message": "Update dependency org.assertj:assertj-bom to v3.25.2 (#6172)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-25T17:36:21-06:00",
          "tree_id": "09005ebe7662a6a665b2c78643304406708afcdc",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/91a5dd9e8a347455423b9cc672d4653e95792b2b"
        },
        "date": 1706230665590,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7217.737892407067,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8283.058406545759,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12771.83076067627,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16524.79119458832,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16761.57759701694,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685895.4080528195,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.858826988693,
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
          "id": "4657255d7b63dce9aa96aa6bf23b8e62df254edf",
          "message": "Update spotless packages to v6.25.0 (#6169)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-26T10:08:24-06:00",
          "tree_id": "0d9c48403a48765c9176fcd036a45bf575c696b4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4657255d7b63dce9aa96aa6bf23b8e62df254edf"
        },
        "date": 1706289217291,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7223.275711520659,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8360.83160879445,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13476.450489818959,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18581.226346383424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16632.16029801187,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675971.4410267468,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8600.814658599871,
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
          "id": "4a66431280b1749f39efdac4edbc801f7f1b3405",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.32.0 (#6165)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-26T10:42:03-06:00",
          "tree_id": "c49862a6ee21083ffad25f535c4b097af6c6a469",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4a66431280b1749f39efdac4edbc801f7f1b3405"
        },
        "date": 1706289437102,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7223.6947384111845,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8445.633058424823,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13564.199485868958,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17824.813148328514,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13808.596639722591,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691716.7028747643,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8633.841686591386,
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
          "id": "90e2fb4e38a936400c80d6f0026afacf597169bb",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.4 (#6176)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-26T10:42:21-06:00",
          "tree_id": "0f8d2249883e2caa42b10714607113f86f0c88e2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/90e2fb4e38a936400c80d6f0026afacf597169bb"
        },
        "date": 1706289731938,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7112.313325897519,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8204.77114919561,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13455.377523483001,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18221.75137768345,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13732.943264278994,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1679395.142884335,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8558.712611775123,
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
          "id": "f123d789097d714ed94f76d98e87985067a7b301",
          "message": "Update plugin com.gradle.enterprise to v3.16.2 (#6175)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-01-26T10:57:54-06:00",
          "tree_id": "2647550a1709d2072f23e3ebfe6dc4de924366e1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f123d789097d714ed94f76d98e87985067a7b301"
        },
        "date": 1706290031373,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7132.664169805129,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8466.372124831432,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13403.016165143354,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17724.51383347595,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15730.404888260202,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678277.0042309563,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8566.33143507149,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "196905916c539c375023f2e85efef12e128c2274",
          "message": "Include trace flags in otlp marshaller (#6167)",
          "timestamp": "2024-01-31T09:45:28-06:00",
          "tree_id": "aa8b148550137ced4a409878276a7305d1516ba4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/196905916c539c375023f2e85efef12e128c2274"
        },
        "date": 1706729072104,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7166.690661089995,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8457.567800092456,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12452.540806052177,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17138.958132327352,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16981.42652913718,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683218.5081595131,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.586123943338,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0aa223dcaf0e7d4fd1f854ec3dd99f1e12d5163d",
          "message": "Memory mode: Adding support for synchronous instruments - Counter (#6182)",
          "timestamp": "2024-01-31T09:47:00-06:00",
          "tree_id": "930797b42aea2957c4664000615f4847d6bc7223",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0aa223dcaf0e7d4fd1f854ec3dd99f1e12d5163d"
        },
        "date": 1706729309846,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7144.430309391698,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8309.903305455773,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12871.490564944574,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18196.294923689675,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16788.790631628573,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681231.3102617736,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8574.972638925648,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8684882c06b91600ce52baaaefe1242f34aa45b8",
          "message": "Add Compressor SPI support to OtlpGrpc{Signal}Exporters (#6103)",
          "timestamp": "2024-01-31T10:59:48-06:00",
          "tree_id": "60ca42c45af9c5efee26bbbe3d28283f90a6bc14",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8684882c06b91600ce52baaaefe1242f34aa45b8"
        },
        "date": 1706734292493,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7016.903647459499,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8235.540676435561,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12819.193275472415,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21044.868036148786,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 12753.131010367395,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686935.7726279304,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8607.10545434843,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "gregor.zeitlinger@grafana.com",
            "name": "Gregor Zeitlinger",
            "username": "zeitlinger"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ecbd1f9d3d8d131588dec983c1fe8474bb3808e1",
          "message": "header values are be expected to be W3C baggage encoded (#6164)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-02-01T15:00:56-06:00",
          "tree_id": "5d074f6f368cdbcd1101c685091d9ecdec46cfd6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ecbd1f9d3d8d131588dec983c1fe8474bb3808e1"
        },
        "date": 1706859285197,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7108.826855254561,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8690.095800825495,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13215.803583991797,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17787.95275715586,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16810.772179904572,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684699.2995945443,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8581.91396749867,
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
          "id": "6b613ddd364b0b09e8477319bb8e267760837782",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.33.0 (#6184)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:01:31-06:00",
          "tree_id": "25c78056e8b84608908722d05760deb33bfad0ae",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6b613ddd364b0b09e8477319bb8e267760837782"
        },
        "date": 1706859506444,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7079.007524686993,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8755.286615782712,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13182.299655997505,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15931.653017155,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16847.430184236968,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691122.6311813283,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8583.012254075516,
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
          "id": "697ae617bee06c91ab325bcfe1a806e3c2007a4f",
          "message": "Update gradle/wrapper-validation-action action to v2 (#6185)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:01:52-06:00",
          "tree_id": "31bb8b1ecdd31b5946850b31daf74a03d8f11ff1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/697ae617bee06c91ab325bcfe1a806e3c2007a4f"
        },
        "date": 1706859723436,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7100.685045027623,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8655.410635222857,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12340.699758045543,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16820.298503147125,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14281.31607334554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691565.9795020171,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8621.504578979215,
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
          "id": "c91f3f427e27e6ae257e214ddcd6f47e65976299",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.27.0 (#6181)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:02:12-06:00",
          "tree_id": "439d34fec8f430b9f3c9ec98331616c6fbbfc4a4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c91f3f427e27e6ae257e214ddcd6f47e65976299"
        },
        "date": 1706859948692,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7166.625794820603,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8375.160507561186,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13578.896948344633,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17212.536279093525,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14510.413632869648,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685559.7941167194,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.037654407293,
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
          "id": "4f9b7314f21d2d01867c6c04bd06158ba1334e12",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.22 (#6180)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:02:26-06:00",
          "tree_id": "ffa30edb00b68b1604b799e67ee3171e961b4e2b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4f9b7314f21d2d01867c6c04bd06158ba1334e12"
        },
        "date": 1706860199539,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7128.580668502851,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8482.631482820436,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13030.265682659581,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18242.75944785696,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16462.56312692495,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687519.9970027525,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8603.117270252254,
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
          "id": "8e2a99a67d8da79f1987a959f7771e2a4ca9685a",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.5 (#6177)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:02:53-06:00",
          "tree_id": "b6769e513fa94f59b9fb76dd7507f6ea4ff04f51",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8e2a99a67d8da79f1987a959f7771e2a4ca9685a"
        },
        "date": 1706860489493,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7101.133867201026,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8627.029812796301,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12971.098086105736,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18114.562779591964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16603.20245867161,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683458.85946131,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8562.323824529865,
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
          "id": "e9266c8575c01724a27a37b669389bec762af4fb",
          "message": "Update dependency checkstyle to v10.13.0 (#6178)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-01T15:03:06-06:00",
          "tree_id": "9d4450ec1d5db0f7451b1de07dca90c1bfdfff4f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e9266c8575c01724a27a37b669389bec762af4fb"
        },
        "date": 1706860765480,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7270.8454528940265,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8600.700313915162,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12780.983759480576,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17384.29272875544,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16239.537933981977,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677287.358639554,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8564.739180075592,
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
          "id": "eba8b847e904d1d5b3c4641d517ecd9359287131",
          "message": "Update dependency io.grpc:grpc-bom to v1.61.1 (#6190)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-03T20:49:18-08:00",
          "tree_id": "a51221fad1eda7e1e113efc07fcd6923281c0373",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/eba8b847e904d1d5b3c4641d517ecd9359287131"
        },
        "date": 1707022385679,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7190.99126606827,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8079.376025663392,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12983.098007157989,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16235.651794810885,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13610.797581935563,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687281.224027849,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.28329392191,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "2d74bf263a703a739ffb6a23179089cd12b52cb3",
          "message": "Define CODECOV token (#6186)",
          "timestamp": "2024-02-05T17:44:17-06:00",
          "tree_id": "062022cf46ce4e58f93074d949ca710d7ae795ac",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2d74bf263a703a739ffb6a23179089cd12b52cb3"
        },
        "date": 1707183938826,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7194.00304934172,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8514.257229050769,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12421.242560156326,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17283.87863657431,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14992.740161473892,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687321.8507157944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8643.913851771089,
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
          "id": "3e7302e742227d4526b765621a933d748012c906",
          "message": "Update codecov/codecov-action action to v4 (#6183)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-05T17:44:38-06:00",
          "tree_id": "1979f38876b73b12b850cdb2cc99969cb694713b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3e7302e742227d4526b765621a933d748012c906"
        },
        "date": 1707184156446,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7160.642011061901,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8449.660305014957,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13709.675624158572,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17345.63497249742,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13807.030581938194,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691360.1416412722,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8637.329193816684,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bae5718191bff429494b55833e2113b2b9427615",
          "message": "Add Span#addLink, for adding link after span start (#6084)\n\nCo-authored-by: John Watson <jkwatson@gmail.com>",
          "timestamp": "2024-02-07T13:04:52-06:00",
          "tree_id": "4081774a3617b76aec3d6db4e4d09e0e71ff0253",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bae5718191bff429494b55833e2113b2b9427615"
        },
        "date": 1707333745323,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7308.634600698566,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8562.619608779167,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12785.437642383253,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18124.51752957589,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15027.974371627037,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681132.967325284,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.820528731432,
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
          "id": "9e6d4a8dc3ad097bc24c092e46fc025eec5d209e",
          "message": "Update dependency org.jctools:jctools-core to v4.0.3 (#6203)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T13:05:08-06:00",
          "tree_id": "5a7136edc68f5359140cc8acd74dfca4c1f54fba",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9e6d4a8dc3ad097bc24c092e46fc025eec5d209e"
        },
        "date": 1707333969524,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7184.9121137023185,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8234.21613019938,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13408.447602791264,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17249.596579890836,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14450.436347365912,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685558.8864108068,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.42878249765,
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
          "id": "715fbd678a56354bc322997ed7f8bc4bab71fc5b",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.27.1 (#6202)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T13:05:24-06:00",
          "tree_id": "c19f1ce0a2cde4435fd784a5ca241271ffa7edb9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/715fbd678a56354bc322997ed7f8bc4bab71fc5b"
        },
        "date": 1707334190545,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7256.910148344133,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8680.340910158993,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12331.52479010206,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18603.245079409855,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16372.23046580589,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691088.8832051405,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.734594060574,
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
          "id": "001fa1cd38622de76bc6636ed9fa096d5b8b65ba",
          "message": "Update slf4j monorepo to v2.0.12 (#6199)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T13:05:55-06:00",
          "tree_id": "186baf8f7a95dc4d09fae665a7705604d6f6b479",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/001fa1cd38622de76bc6636ed9fa096d5b8b65ba"
        },
        "date": 1707334412432,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7246.4620020345765,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8342.14654547379,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12071.377770088115,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18420.229770725895,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16390.627573858794,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689892.8183040838,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8628.563788016505,
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
          "id": "63b39e37e995bba4bb50efcd4e24c1eb25d25d55",
          "message": "Update dependency org.assertj:assertj-bom to v3.25.3 (#6193)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T13:06:10-06:00",
          "tree_id": "6eb38bc9f6d15452cb99624c3cb8016982815726",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/63b39e37e995bba4bb50efcd4e24c1eb25d25d55"
        },
        "date": 1707334638426,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7384.363957717962,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8590.021435223625,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12847.98299758372,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17083.928902714462,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16407.640187853227,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686001.1068643841,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8559.334766304153,
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
          "id": "3265f3289de680d931f07e4aa32857acf569468d",
          "message": "Update gradle/wrapper-validation-action action to v2.1.0 (#6200)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T15:46:59-06:00",
          "tree_id": "07b8122a3e225921f743ba60a30a69d254eba600",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/3265f3289de680d931f07e4aa32857acf569468d"
        },
        "date": 1707343895944,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7302.206232335465,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8464.498643512903,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13564.319801818641,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17637.82665784173,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16188.962736395406,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687461.2377767493,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.240185293287,
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
          "id": "82bf8c36235d017db1ef1ab2e3a91e573038aaf0",
          "message": "Update dependency org.junit:junit-bom to v5.10.2 (#6192)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T15:47:28-06:00",
          "tree_id": "ea4249173165349a2a04c215ff264fd458e2a6b0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/82bf8c36235d017db1ef1ab2e3a91e573038aaf0"
        },
        "date": 1707344121126,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7322.455031220136,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8836.693759793923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13155.1758228773,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17601.067271000025,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15275.990462016049,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690347.222166373,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.469874949891,
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
          "id": "fe7a1c58f8360bbffbb5b5a1cf73212c48d39793",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.6 (#6189)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T15:47:46-06:00",
          "tree_id": "56fd07f1da8b6a7b38ba43113b403318892302f7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fe7a1c58f8360bbffbb5b5a1cf73212c48d39793"
        },
        "date": 1707344412976,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7329.626331040214,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8738.684017550364,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13340.37040974911,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17030.888801986883,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16574.432943927048,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677415.8092119158,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.721519575212,
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
          "id": "b8b804b68bf8ae6aa49d9f83bcb89875a2684090",
          "message": "Update plugin org.graalvm.buildtools.native to v0.10.0 (#6188)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T15:48:56-06:00",
          "tree_id": "cf2fef908167211facb175ce6f4d656ee2b3d001",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b8b804b68bf8ae6aa49d9f83bcb89875a2684090"
        },
        "date": 1707344634038,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7375.344407855549,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8258.274131122587,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13398.233239706416,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16231.042599997676,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16465.646918671326,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689489.5371735662,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8627.783679271994,
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
          "id": "c12bb736caeebd945e5df30852e84c8e4c9d2789",
          "message": "Update dependency gradle to v8.6 (#6191)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-07T15:49:10-06:00",
          "tree_id": "28e7dd30a60342fa95422fb8233be96cd8ca1c54",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c12bb736caeebd945e5df30852e84c8e4c9d2789"
        },
        "date": 1707344959011,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7165.289630673407,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8292.405738909194,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13301.902361889603,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16326.972507165137,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13681.846670997762,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682023.0234104376,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8556.881788474244,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c12779d960918fdf7881a6d81d215e51e509ebd7",
          "message": "Memory mode: Adding support for synchronous instruments - Last Value aggregation (#6196)",
          "timestamp": "2024-02-08T09:29:16-06:00",
          "tree_id": "23b8319cffe229f0b889b9bcce498ff64dcb4bf7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c12779d960918fdf7881a6d81d215e51e509ebd7"
        },
        "date": 1707406428240,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7315.153153922131,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8836.444122631547,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12865.237900366561,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16901.80195936528,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13723.522432793668,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687665.949526167,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8618.685293096278,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "austinpederson@protonmail.com",
            "name": "Austin Pederson",
            "username": "apederson94"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "c0b73f5218d3b0867c01d07a456d311da6f09a20",
          "message": "#6207 Ensure Span Status Cannot Be Updated After StatusCode.OK Is Set (#6209)",
          "timestamp": "2024-02-08T13:46:28-06:00",
          "tree_id": "a54497b911f317d8f06e93af20b77e8261bb6f9a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c0b73f5218d3b0867c01d07a456d311da6f09a20"
        },
        "date": 1707422092484,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7243.522803991855,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8612.480426589289,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12319.465881783508,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16665.18363310116,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16537.521874251517,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685796.1198613755,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8605.868310983551,
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
          "id": "988dccaa5e0a86b3413029c9f04e9ee9e3370be0",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.5 (#6212)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-08T16:08:53-06:00",
          "tree_id": "c87cee303fd283946f9f92702b4ad97d9de6b1a0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/988dccaa5e0a86b3413029c9f04e9ee9e3370be0"
        },
        "date": 1707431248757,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7387.4185389100785,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8504.71798405558,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13606.51158145741,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17542.689752042646,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14765.954378966248,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684816.8950308897,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.93930587292,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "a5d20654014a190ea6b58de45a17623a8d2dc30a",
          "message": "Allow Prometheus exporter to add resource attributes to metric attributes (#6179)",
          "timestamp": "2024-02-08T16:09:13-06:00",
          "tree_id": "130206e427193278ed68c0552efd081aa0be1d43",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a5d20654014a190ea6b58de45a17623a8d2dc30a"
        },
        "date": 1707431472328,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7275.937873074806,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8409.748616968924,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12616.568416522296,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17719.844977747034,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15353.539722876114,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691503.7894928176,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8674.654179128145,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e9e1feeae0d9b7e04a02e316a9294d96cf250fe7",
          "message": "Prepare 1.35.0 release (#6213)\n\nCo-authored-by: Trask Stalnaker <trask.stalnaker@gmail.com>",
          "timestamp": "2024-02-09T07:59:03-06:00",
          "tree_id": "a17d25a8fa4522fc8fa888070b5c55bcf47cf99c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e9e1feeae0d9b7e04a02e316a9294d96cf250fe7"
        },
        "date": 1707519468014,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7324.819272400789,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8709.188762638121,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12733.002818849543,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17209.75025236007,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15312.394485811523,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683181.3307762258,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8606.53236324944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "695ed5350b2ef218c38087a627549f4de7b86c22",
          "message": "Update version to 1.36.0 (#6216)",
          "timestamp": "2024-02-09T09:56:03-06:00",
          "tree_id": "261243336aac8aa2756e24afb44dc7f7e43b012a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/695ed5350b2ef218c38087a627549f4de7b86c22"
        },
        "date": 1707521589416,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7283.401682093533,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8299.518089787442,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12793.343047096225,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16154.49347993361,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13852.756088014252,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687299.5511365063,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8589.849390632426,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "0e84508905a9e84ff15fd98444483499c9953b2e",
          "message": "Fix flaky InstrumentGarbageCollectionBenchmarkTest (#6221)",
          "timestamp": "2024-02-12T09:35:03-06:00",
          "tree_id": "0dd8e2c0a976b042e3e0da7d8d942bcfb7e1282b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/0e84508905a9e84ff15fd98444483499c9953b2e"
        },
        "date": 1707752321944,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7295.882353975287,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8731.536788264366,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12894.90039097695,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21914.64301039678,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13855.52735914617,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688497.7989352804,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8642.845426718126,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "48d41d3a5aa27ff8e5e4033ef0c8c3ba0e53f683",
          "message": "Update docs post 1.35.0 release (#6218)",
          "timestamp": "2024-02-12T10:15:12-06:00",
          "tree_id": "ae2ea28046670c1de843b6bbb77788b6d98312c5",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/48d41d3a5aa27ff8e5e4033ef0c8c3ba0e53f683"
        },
        "date": 1707754728390,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7327.784400084294,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8312.757976613422,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13762.060279809832,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16662.188991134528,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14896.92150170833,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687259.2269605559,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8615.8674132943,
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
          "id": "a903fa849f242eb708c77e443f2b92f61db3c268",
          "message": "Update errorProneVersion to v2.25.0 (#6236)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:18:06-06:00",
          "tree_id": "0062fc172471dba5f5cb93054d47f72f1a7114fd",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a903fa849f242eb708c77e443f2b92f61db3c268"
        },
        "date": 1708183339447,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7303.433451631319,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8308.5934968091,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12823.6253290494,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17093.677275773698,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15976.960562078584,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685186.0605987657,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8547.543582186718,
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
          "id": "82e4354be94775f6baccc2dfbf31036b119706c6",
          "message": "Update dependency com.google.protobuf:protobuf-bom to v3.25.3 (#6234)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:18:22-06:00",
          "tree_id": "e88103854a2c083f5718ece72bce702c7259c6f3",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/82e4354be94775f6baccc2dfbf31036b119706c6"
        },
        "date": 1708183588937,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7205.846212694395,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8379.114917431765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12974.683179395366,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17415.387555069297,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16544.88486481325,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680259.6433175553,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8610.261244962388,
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
          "id": "620a26485968009c15b6ba8e4b58220c20cd4d18",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.23 (#6232)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:18:40-06:00",
          "tree_id": "a9e9c7c1c1484ea8185213127e61fc9aed1571fb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/620a26485968009c15b6ba8e4b58220c20cd4d18"
        },
        "date": 1708183838116,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7327.407748841718,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8477.440307537354,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13196.013824788743,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17019.002530001053,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16134.705925336413,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687095.1659911007,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.779759098421,
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
          "id": "f2b92832eb9a5159168b3ef307682c122cfc39b2",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.34.0 (#6229)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:19:02-06:00",
          "tree_id": "2ec817d4402f2f3b324cf3632851427ac54998ea",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f2b92832eb9a5159168b3ef307682c122cfc39b2"
        },
        "date": 1708184061498,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7352.944538930497,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8373.089122155794,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12891.161612911495,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17421.773915021913,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15963.071339079204,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686276.0734144053,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8625.264146415178,
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
          "id": "11391d8b1ad9d936148d8ffb592a9d8eb4698b45",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.7 (#6223)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:20:39-06:00",
          "tree_id": "69703c31707733bf2fde219f7ebbd1108195c64c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/11391d8b1ad9d936148d8ffb592a9d8eb4698b45"
        },
        "date": 1708184353818,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7221.1067083834105,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8633.107878428476,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11805.536171209656,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15341.829726285141,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14596.296326969426,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681932.7457254466,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8590.857094176234,
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
          "id": "046baf9ca94924e5c6c3a47e7437675a49d7a82f",
          "message": "Update dependency com.squareup.okio:okio-bom to v3.8.0 (#6219)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:20:56-06:00",
          "tree_id": "86606357e2c7b844c8879455b9f03fab269a25d9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/046baf9ca94924e5c6c3a47e7437675a49d7a82f"
        },
        "date": 1708184577209,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7214.273567560211,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8445.256803954879,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11378.018653130097,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18223.710440252442,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14705.874450757367,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690264.4281359732,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.643635899685,
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
          "id": "b0ec50575e4c42edafcd4295635afb01c9622563",
          "message": "Update gradle/wrapper-validation-action action to v2.1.1 (#6220)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:21:08-06:00",
          "tree_id": "e9f236b2db3a83c257e44af5e40f4ce542995dc1",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b0ec50575e4c42edafcd4295635afb01c9622563"
        },
        "date": 1708184791643,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7316.162292210572,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8467.534210899506,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12158.973342534955,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17182.82773859382,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15972.919785709402,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686094.3616492874,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.518141982531,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "47a390076f3de4325c48459fcc4d9da7ee8dde71",
          "message": "Fix omission in readme overview (#6120)",
          "timestamp": "2024-02-17T09:33:48-06:00",
          "tree_id": "cccd38a809d436a9da00c970f8ca6edbfaf5c507",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/47a390076f3de4325c48459fcc4d9da7ee8dde71"
        },
        "date": 1708185006484,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7354.381531922078,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8468.426435218204,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12860.073026632806,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18211.474576862594,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13014.252256925902,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690101.8455213036,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8629.591381969261,
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
          "id": "2dfbc242f7b9f066a224f8a60ed82d18b55476c7",
          "message": "Update gradle/gradle-build-action action to v3 (#6238)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:43:40-06:00",
          "tree_id": "e04aa2f783079e5a0d45989f0f18b0dd591d6ae0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2dfbc242f7b9f066a224f8a60ed82d18b55476c7"
        },
        "date": 1708185225768,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7356.80961855731,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8273.915260142187,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12900.878239180687,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15469.583741597628,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16592.77808332934,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690000.8098102864,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8634.380135687788,
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
          "id": "5eaf4b290ebf3f703d138fe8c636b040608d54f4",
          "message": "Update dependency net.ltgt.gradle:gradle-nullaway-plugin to v2 (#6237)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-17T09:46:25-06:00",
          "tree_id": "084984a66f4f75ebe4be60d5dfba85c82b650f3f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5eaf4b290ebf3f703d138fe8c636b040608d54f4"
        },
        "date": 1708185519575,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7273.881231958163,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8374.859518663996,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12069.718610211365,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15703.269362069748,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16766.500106176616,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682707.4762364137,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8483.848817137967,
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
          "id": "77bb43942281ca2a5531ee6ef5fb6648505dc6a0",
          "message": "Update dependency io.zipkin.reporter2:zipkin-reporter-bom to v3.3.0 (#6230)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-02-20T16:24:03-06:00",
          "tree_id": "f4337a96c2aed1dee88759c1bcb63eb8b85e908d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/77bb43942281ca2a5531ee6ef5fb6648505dc6a0"
        },
        "date": 1708468065037,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7349.18433569943,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8889.787899474499,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12712.600743183868,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19126.64840736264,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14845.850060896686,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687692.0490445397,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.84373867205,
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
          "id": "01ff943683ad9d5684a0bc2bd6b60fe929d6736f",
          "message": "Update dependency io.zipkin.brave:brave-bom to v6.0.1 (#6228)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-23T18:46:48-08:00",
          "tree_id": "0e572c55c3abb28eec066b163e6bd5547f076856",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/01ff943683ad9d5684a0bc2bd6b60fe929d6736f"
        },
        "date": 1708743032150,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7312.844037855241,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8654.29742159319,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 14115.717785505487,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15824.660383808685,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15145.839642812454,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687927.674446917,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.24745928069,
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
          "id": "31fc7d0f770774ac0abf4cfc5f7aca8015a364d9",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.7 (#6246)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-23T18:47:17-08:00",
          "tree_id": "c0e2b8be15691014a2bff3d60ef224fe11a8130f",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/31fc7d0f770774ac0abf4cfc5f7aca8015a364d9"
        },
        "date": 1708743253728,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7307.411018020089,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8459.960000580788,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12927.292740358398,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18970.140176077482,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14026.404453418869,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689486.9612747002,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8604.934252926125,
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
          "id": "8e298b9b13017969c2dbf17dbcfb2e99f4e263fb",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.6 (#6242)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-23T18:48:07-08:00",
          "tree_id": "34ced8828373eb802dd9985619d4e0105ec6e084",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8e298b9b13017969c2dbf17dbcfb2e99f4e263fb"
        },
        "date": 1708743482510,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7431.586012785252,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8439.640631166618,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12968.354740753146,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17105.797036834185,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16233.499766132014,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684860.6194601518,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.765149491857,
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
          "id": "dec4e9b583f4faaf9797435cc1dccb7899d0159f",
          "message": "Update plugin org.graalvm.buildtools.native to v0.10.1 (#6239)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-23T18:48:19-08:00",
          "tree_id": "5e74b1a683c55858c214ea2de9ae7a9e4203d439",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/dec4e9b583f4faaf9797435cc1dccb7899d0159f"
        },
        "date": 1708743704352,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7357.482664817689,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8309.494218990116,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12924.612634640507,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16921.363835995424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16500.60066847851,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686838.3755220952,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.130022663558,
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
          "id": "4eb05c7c007ded16acc7fb234252ba4d16aa6641",
          "message": "Update dependency io.netty:netty-bom to v4.1.107.Final (#6227)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-02-23T19:51:21-08:00",
          "tree_id": "f20cdb8b7fdcad2bb0f21107e7c3fc6d2caecc38",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4eb05c7c007ded16acc7fb234252ba4d16aa6641"
        },
        "date": 1708746906089,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7336.680111968122,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8590.013919092245,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13106.647435831923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16746.0276062069,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15261.740331242865,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683121.983443468,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8608.783208227605,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "jbley@splunk.com",
            "name": "John Bley",
            "username": "johnbley"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "96fe54fc16a85fd0a10c0d824679291d06941bab",
          "message": "Fault in SdkSpan.events (#6244)\n\nCo-authored-by: John Watson <jkwatson@gmail.com>",
          "timestamp": "2024-02-23T19:51:41-08:00",
          "tree_id": "1062c3048fa361c0d3d901e45ac4ba1c56761263",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/96fe54fc16a85fd0a10c0d824679291d06941bab"
        },
        "date": 1708747127295,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7363.817474105615,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8370.068338807967,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13382.851724484368,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18592.867581023056,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15182.411374448033,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686545.1671798856,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8630.532652821868,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1d4a2f4a503244244197b1c3ab38b9b2f3237d50",
          "message": "Add connectTimeout configuration option OtlpGrpc{Signal}Exporters (#6079)",
          "timestamp": "2024-02-23T20:26:32-08:00",
          "tree_id": "f166e92965d3f74c901dc7fd6b99a016c613f292",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1d4a2f4a503244244197b1c3ab38b9b2f3237d50"
        },
        "date": 1708749020721,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7287.154721424357,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8293.519336003408,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13103.887484675794,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18007.200978017892,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15541.199482069831,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688914.5017739206,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8592.096437977514,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6afb816f9eccf2524dcb7dc080a92dc97186f251",
          "message": "Add note about draft PRs to CONTRIBUTING.md (#6247)",
          "timestamp": "2024-02-28T16:53:34-06:00",
          "tree_id": "3eaf118cbcd7850ada9be6bbe2f1967f6d338ee0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6afb816f9eccf2524dcb7dc080a92dc97186f251"
        },
        "date": 1709161031309,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7250.848453853073,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8764.422190241898,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13287.15667669774,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 21753.51821662916,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14411.32686602954,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686434.2574446548,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.558788165588,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "62a4810145eec10abda6b023a70f7e9252ca217c",
          "message": "Added MetricReader customizer for AutoConfiguredOpenTelemetrySdkBuilder (#6231)",
          "timestamp": "2024-02-29T08:59:58-06:00",
          "tree_id": "882bec6cd3fc5233c387b98f8df2208eeebf2941",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/62a4810145eec10abda6b023a70f7e9252ca217c"
        },
        "date": 1709219026806,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7284.3674646603195,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8712.519144642145,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13040.335755725513,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18439.289127115702,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16817.08950925708,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1689070.7805514212,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8638.161612632557,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "limolkova@microsoft.com",
            "name": "Liudmila Molkova",
            "username": "lmolkova"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "25afc3e91080ea5099cb5a6a49492c38a38c0529",
          "message": "Return AutoConfiguredOpenTelemetrySdkBuilder instead of the base type (#6248)",
          "timestamp": "2024-03-06T08:40:28-06:00",
          "tree_id": "615d51b08b1e3c62f9bb2e53dca5c801a4a8ab08",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/25afc3e91080ea5099cb5a6a49492c38a38c0529"
        },
        "date": 1709736322050,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7236.289694052858,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8695.609345541001,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 14162.91877650923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16920.478691455766,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14930.408632640925,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683554.9959034915,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8556.048572760681,
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
          "id": "9f8ed8468a077798eeac96916a2a36e87c18f45d",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.24 (#6269)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:55:24-06:00",
          "tree_id": "b98db594e761efc274584763e3859f18d0a56f8b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/9f8ed8468a077798eeac96916a2a36e87c18f45d"
        },
        "date": 1709737177153,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7203.3591348303435,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8520.369787874968,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10977.52455715989,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16931.834507026622,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15587.756812133104,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678563.025481999,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.291277311102,
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
          "id": "ff179ddc6b6a09e0c09672d25026099f3bcff905",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.15.8 (#6267)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:55:39-06:00",
          "tree_id": "7452ba4f6e16cf990d7e9722f902b279f20d6a9c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ff179ddc6b6a09e0c09672d25026099f3bcff905"
        },
        "date": 1709737396104,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7232.196698430161,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8689.20732465144,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11618.962626893093,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18830.086143956687,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17077.447259867145,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686867.59351799,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8595.929356025632,
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
          "id": "12d3e0f1f0ae0542a501cce930fc742a495a04ee",
          "message": "Update dependency io.zipkin.brave:brave-bom to v6.0.2 (#6259)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:55:56-06:00",
          "tree_id": "040d420949c8ac44c0c437d22a82d4f665236371",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/12d3e0f1f0ae0542a501cce930fc742a495a04ee"
        },
        "date": 1709737615734,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7303.658766137584,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8265.45121102717,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12963.515130411219,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16757.489761729426,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16233.67954342693,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691116.3470073,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8606.329773779762,
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
          "id": "601d2885ff77665a8522643f77f1f30afa0e3841",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.36.0 (#6265)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:56:15-06:00",
          "tree_id": "659b39197fefedb64503c4b2bbabb2ce9ab87ce5",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/601d2885ff77665a8522643f77f1f30afa0e3841"
        },
        "date": 1709737835906,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7297.8146297819285,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8787.192186602562,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12632.084911705455,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19124.693788846584,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16097.424248681962,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687661.1814868418,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8632.020044902307,
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
          "id": "5ef2cb95739245b6d31d9f3084334af912f56989",
          "message": "Update dependency checkstyle to v10.14.0 (#6261)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:56:30-06:00",
          "tree_id": "f0638718b932c391d8eb539db5595e8ec6c0bacd",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5ef2cb95739245b6d31d9f3084334af912f56989"
        },
        "date": 1709738114451,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7283.662164839958,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8520.022725523755,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13322.328177725323,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18745.691488646084,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16499.59385037484,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1675591.121457156,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8542.80939773398,
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
          "id": "5eeb733dbdc9b5c39ef0a51cbf91bdf9d3f4dc39",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.27.2 (#6249)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:56:48-06:00",
          "tree_id": "fd22e4d013d43450b83b9e51ce0988d5c96fba1b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/5eeb733dbdc9b5c39ef0a51cbf91bdf9d3f4dc39"
        },
        "date": 1709738336341,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7234.692633263801,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8292.998277892442,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11919.398608288097,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18596.295799799027,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14877.839880351701,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687227.489579641,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8627.991555545475,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "gregor.zeitlinger@grafana.com",
            "name": "Gregor Zeitlinger",
            "username": "zeitlinger"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "56c11e529e221da68a7aaaee7946247e78c2a35e",
          "message": "add ComponentLoader to support more auto configuration scenarios (#6217)",
          "timestamp": "2024-03-06T08:57:52-06:00",
          "tree_id": "06a7f4a08a4223dc7478808ec23ead08d7e1b6d0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/56c11e529e221da68a7aaaee7946247e78c2a35e"
        },
        "date": 1709738630393,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7189.970562091601,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8856.130253019079,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13628.486540834052,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15970.955986168405,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15045.057335276919,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1670653.0946791698,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8538.650119817314,
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
          "id": "96bed03986217c5f8797ec5c7cab88a04f5134fe",
          "message": "Update plugin de.undercouch.download to v5.6.0 (#6262)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:58:25-06:00",
          "tree_id": "c85ea153d27b03247c89e9879243dfd50c2d65a9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/96bed03986217c5f8797ec5c7cab88a04f5134fe"
        },
        "date": 1709738853794,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7249.774986997206,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8659.55147982757,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12601.871408186784,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15120.323288193886,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14905.326384070799,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683922.809005448,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8592.033229289358,
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
          "id": "cd52950fae78c2880b5b0ed1e61672f54d41707a",
          "message": "Update dependency io.grpc:grpc-bom to v1.62.2 (#6254)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:58:44-06:00",
          "tree_id": "a7fed483b02eb9a7c516f553df574db3b71c449b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/cd52950fae78c2880b5b0ed1e61672f54d41707a"
        },
        "date": 1709739088081,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7262.997009144892,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8333.185294082727,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13495.994037528862,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17241.157250584867,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15094.973692128235,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680645.9603947562,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8554.944287041148,
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
          "id": "f24acbdd7fbf1f199872f54d25cf2ca2340816d6",
          "message": "Update dependency org.testcontainers:testcontainers-bom to v1.19.7 (#6271)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-06T08:59:00-06:00",
          "tree_id": "75d3936e30341d33dce38f18148856b919c5d6bf",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f24acbdd7fbf1f199872f54d25cf2ca2340816d6"
        },
        "date": 1709739314963,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7254.513159675767,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8321.411689525039,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12033.727905161071,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17355.257258853017,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16115.597264112188,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687273.2041705947,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8612.808222010435,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "e41470be4366d4fadb2cf0f810441dda21ce6c5e",
          "message": "Add basic proxy configuration to OtlpHttp{Signal}Exporters (#6270)\n\nCo-authored-by: Marc Schumacher <schumi@zalando.de>",
          "timestamp": "2024-03-07T06:48:36-06:00",
          "tree_id": "fa5761f627f9d1f4c04ee60c1a2c55d29af84c2d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e41470be4366d4fadb2cf0f810441dda21ce6c5e"
        },
        "date": 1709815963444,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7229.050581391452,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8603.089810160294,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12886.711529747548,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17697.595840438807,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14613.468703739036,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680910.8107828186,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8581.10631525559,
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
          "id": "c3ed1d5ee564fd9b530c0753ab6b80b7ae50f932",
          "message": "Update dependency org.jetbrains.kotlin:kotlin-gradle-plugin to v1.9.23 (#6275)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-07T06:49:27-06:00",
          "tree_id": "ae12030c70f84029501cc8b51b96de771309e1a2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/c3ed1d5ee564fd9b530c0753ab6b80b7ae50f932"
        },
        "date": 1709816266521,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7267.016253762679,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8309.478570528747,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12326.266730033212,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17747.38673293503,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13815.791371061938,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1685635.4846195944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8573.52734126522,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f032fc670380b1b7e138e01e88f10c9e898cbe3d",
          "message": "Prepare 1.36.0 (#6276)",
          "timestamp": "2024-03-08T06:44:26-06:00",
          "tree_id": "44383fe0a1a54630bb89bf453f44a23238bad99c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f032fc670380b1b7e138e01e88f10c9e898cbe3d"
        },
        "date": 1709902171349,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7333.689358043298,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8406.515028725156,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12767.128604209644,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16857.251725537022,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14897.764000442508,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690270.2427634574,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8633.346371766807,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "107717825+opentelemetrybot@users.noreply.github.com",
            "name": "OpenTelemetry Bot",
            "username": "opentelemetrybot"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "05bd972f9005ea8a3aec04d55361915ae6534822",
          "message": "Update version to 1.37.0 (#6279)",
          "timestamp": "2024-03-08T07:03:43-06:00",
          "tree_id": "1b5ddec4f1f2561d081f5e3e9c56ab7c4b57b5bb",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/05bd972f9005ea8a3aec04d55361915ae6534822"
        },
        "date": 1709903318276,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7215.1852233170375,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8684.356757432539,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11901.927320416791,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17137.335355751322,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13516.339832347609,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682355.0323547397,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8601.310763670506,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "fdaf68045428639411ff6008db8baec7574c25ca",
          "message": "Post release 1.36.0 (#6280)",
          "timestamp": "2024-03-08T18:04:49-06:00",
          "tree_id": "7d1c403fd64026f9f6c4a83082ed939b2e6bd5be",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/fdaf68045428639411ff6008db8baec7574c25ca"
        },
        "date": 1709942905441,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7311.764344394652,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8291.858500452563,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13225.20107257096,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16979.0195699991,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14575.200191242271,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690635.5891066021,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8595.010343900201,
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
          "id": "a7423f66760c76f2ab20c4c63ec5859ccb704c77",
          "message": "Update dependency com.fasterxml.jackson:jackson-bom to v2.16.2 (#6281)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-09T16:45:08-08:00",
          "tree_id": "41bee1fdbb0f6326e5825350c43c7714f54790f7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/a7423f66760c76f2ab20c4c63ec5859ccb704c77"
        },
        "date": 1710031741124,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7299.992951715572,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8306.988949392926,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10821.621410926668,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15315.057397699595,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14654.931470907692,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683082.099910149,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8587.833060259834,
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
          "id": "97609a9bdde95afe64e42fa6cca98fc6364fc4a5",
          "message": "Update dependency checkstyle to v10.14.1 (#6283)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-11T13:04:03-07:00",
          "tree_id": "4cc8490ed1e267daf9300a1ba5fce72a21751bc2",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/97609a9bdde95afe64e42fa6cca98fc6364fc4a5"
        },
        "date": 1710187724030,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7213.874609688141,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8707.70669005805,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13320.858165986278,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17262.82776112825,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17269.16512881287,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683037.8784374115,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8564.38576453215,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7655192df504d28da2d2013f2fc49ec44ef6202e",
          "message": "Add autoconfigure console alias for logging exporter (#6027)",
          "timestamp": "2024-03-12T09:43:19-05:00",
          "tree_id": "f53be0122cf6a67ff1d01968da27820604e0bf32",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7655192df504d28da2d2013f2fc49ec44ef6202e"
        },
        "date": 1710254817093,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7259.4289628673305,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8364.757576501945,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13223.928704265887,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16130.612937937043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16698.547762252412,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690571.8439855843,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8625.717828464181,
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
          "id": "7831f2ab2dcaea53fb40d727dc1a722e50ab491d",
          "message": "Update errorProneVersion to v2.26.0 (#6286)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-12T08:03:02-07:00",
          "tree_id": "806c8eac57c0e8cf60d31f49797e3d2ff2c8bc3d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7831f2ab2dcaea53fb40d727dc1a722e50ab491d"
        },
        "date": 1710256037624,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7239.1131100296525,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8344.68989871487,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12364.499448428101,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16898.953736349034,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13835.432380222515,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678335.095004452,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8575.134425066524,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "34b64b1f772f9e6d3874470e86713fc4675abbd1",
          "message": "Remove domain from event api. (#6253)\n\nCo-authored-by: Trask Stalnaker <trask.stalnaker@gmail.com>",
          "timestamp": "2024-03-12T14:27:33-05:00",
          "tree_id": "d2e68f0d35e4a56a1464115e77522997cfaae07a",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/34b64b1f772f9e6d3874470e86713fc4675abbd1"
        },
        "date": 1710272085364,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7285.753080040456,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8659.989184676233,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13177.589513175833,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18183.923674625643,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16797.731158784936,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687998.635990372,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8595.109593558584,
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
          "id": "4a4d9a6a6ca306e718c6036ac6962600cf99cf2c",
          "message": "Update dependency com.fasterxml.jackson:jackson-bom to v2.17.0 (#6292)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-12T19:52:38-07:00",
          "tree_id": "ded57e23cebafb08ee2c5a6b3738ebaeb158eda4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4a4d9a6a6ca306e718c6036ac6962600cf99cf2c"
        },
        "date": 1710298714452,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7259.899207350366,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8265.858673983583,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13036.284848398074,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15454.490488388517,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16511.073215472406,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688607.775630147,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8600.53631263881,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "f83c020d4d2a11f16be8af739790718bb3413cba",
          "message": "Use shared CODECOV_TOKEN (#6291)",
          "timestamp": "2024-03-12T19:55:11-07:00",
          "tree_id": "5b801ebf4e22c5e721ce6a691dc3910125cee78e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f83c020d4d2a11f16be8af739790718bb3413cba"
        },
        "date": 1710298941671,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7340.113970743829,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8828.369322065615,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13139.724947791075,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17169.94385710889,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13444.231251176683,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690966.534797456,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8623.776114336037,
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
          "id": "dde0d6936c4e6e3c1fb16482cd4e9092fa3f65dc",
          "message": "Update dependency org.awaitility:awaitility to v4.2.1 (#6298)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:23:46-07:00",
          "tree_id": "bfd9d09f5ad4c290a5a187c5f0ee8ce3d33d9361",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/dde0d6936c4e6e3c1fb16482cd4e9092fa3f65dc"
        },
        "date": 1710624449009,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7324.746564116139,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8618.381555923148,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10794.475473385926,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16876.773524023905,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16186.897356096146,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1680957.1542410827,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8618.059644891899,
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
          "id": "1f22d1c46eda591006822d83ba5576e655c14d1a",
          "message": "Update dependency com.google.guava:guava to v33.1.0-jre (#6296)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:24:08-07:00",
          "tree_id": "ceb53766897e763dac8d2cc44158e90037ec9979",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1f22d1c46eda591006822d83ba5576e655c14d1a"
        },
        "date": 1710624745564,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7275.636787585707,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8631.985765905178,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11942.728735947518,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16275.819208262319,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15030.547848345192,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682543.9609470814,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8543.942626021524,
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
          "id": "2fc71ea8e34eb9ee6ec64e57dcafa922c248c897",
          "message": "Update dependency com.google.api.grpc:proto-google-common-protos to v2.37.1 (#6299)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:24:28-07:00",
          "tree_id": "ed4bb33ef63f0397a84a728266fee9eb3f3dd5c0",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/2fc71ea8e34eb9ee6ec64e57dcafa922c248c897"
        },
        "date": 1710624969231,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7243.429151497392,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8813.674035164222,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10764.998721314765,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18451.299893240015,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15497.311416243687,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690037.8778295931,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8614.813802244022,
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
          "id": "b0ccf5ec7d8928aa03849b2bad43feb5e608d6f0",
          "message": "Update dependency com.squareup.okio:okio-bom to v3.9.0 (#6293)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:25:00-07:00",
          "tree_id": "b0a91f1eee5389bd31109589f3302db31db9ba23",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b0ccf5ec7d8928aa03849b2bad43feb5e608d6f0"
        },
        "date": 1710625192282,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7240.170999408099,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8371.66442885263,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11542.658986001603,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18322.920412211006,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16607.60635950189,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690651.3634349387,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8631.793201459715,
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
          "id": "970f95f2eceee725d7e7f8ab7a435f2703a590e6",
          "message": "Update dependency com.google.guava:guava-bom to v33.1.0-jre (#6294)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:25:23-07:00",
          "tree_id": "93a7cdb7af4cd6314263cafbaa8cac7665b89c74",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/970f95f2eceee725d7e7f8ab7a435f2703a590e6"
        },
        "date": 1710625445474,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7285.048519218539,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8279.340475407796,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13558.598541142073,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15910.202466480661,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14697.622063303026,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681678.0032447756,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8572.671397740045,
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
          "id": "b2298ab4c0add0b7b36a5feb42505d4de3d54ba0",
          "message": "Update dependency org.owasp:dependency-check-gradle to v9.0.10 (#6300)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-16T14:25:56-07:00",
          "tree_id": "8f2f0739a62920f2c8f2079ee904599d55148f6b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b2298ab4c0add0b7b36a5feb42505d4de3d54ba0"
        },
        "date": 1710625738417,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7352.620891505331,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8587.352786809632,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12570.33653497948,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17202.700722717134,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14829.511883928164,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1676134.2461483944,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8595.278178204659,
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
          "id": "6e3d0873bc1741c361574104ac8fe5d2c8faf3e6",
          "message": "Update dependency checkstyle to v10.14.2 (#6301)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-19T08:25:05-07:00",
          "tree_id": "ee91ba77949be9aeb928b3b300a83e5c6bc60b76",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6e3d0873bc1741c361574104ac8fe5d2c8faf3e6"
        },
        "date": 1710862183574,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7298.319165452182,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8618.778721659419,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12796.576783580305,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17666.70026774899,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16549.525370802927,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683551.454215867,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8366.665461626651,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "75337021+breedx-splk@users.noreply.github.com",
            "name": "jason plumb",
            "username": "breedx-splk"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "8a73f45a2c4521de29c3c60ee75674b9bbf6c43c",
          "message": "Fix copypasta artifact (#6311)",
          "timestamp": "2024-03-20T11:03:20-05:00",
          "tree_id": "91a0da3feede795307abf42be947cf4652ad5c7e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/8a73f45a2c4521de29c3c60ee75674b9bbf6c43c"
        },
        "date": 1710950819810,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7274.996447865368,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8708.031614122043,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13546.97145981959,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17088.289849202803,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14614.483320448897,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1688470.8988931954,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8635.147293142056,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "7926c45a5f8afe11857e632021b29bf6b973f6de",
          "message": "Update jaeger autoconfigure docs to point to OTLP (#6307)",
          "timestamp": "2024-03-20T11:15:21-05:00",
          "tree_id": "00bb0042253d43f6ecaad644b560966cd2ada099",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/7926c45a5f8afe11857e632021b29bf6b973f6de"
        },
        "date": 1710951536863,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7233.22590795866,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8423.223372806115,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12998.282621457749,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17875.48979162801,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16690.25316818524,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691604.218699251,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8617.459495011015,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "44c69ea6b5477b89cd2b3e735707a92d8e69be86",
          "message": "Rename opentelemetry-extension-incubator to opentelemetry-api-incubator, merge opentelemetry-api-events (#6289)",
          "timestamp": "2024-03-21T14:03:07-05:00",
          "tree_id": "ed5c57d94cbc86812b536a029c3bcd80b049a3f9",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/44c69ea6b5477b89cd2b3e735707a92d8e69be86"
        },
        "date": 1711048035347,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7224.528425500472,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8574.463466372454,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13277.868640409011,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16971.693635990214,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15374.59822781361,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683411.4802860892,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8633.421423227424,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "bogdandrutu@gmail.com",
            "name": "Bogdan Drutu",
            "username": "bogdandrutu"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "d8f26b221e35cb529ff48e8fd54c8bd35fd57d9f",
          "message": "Check for Java 17 toolchain and fail if not found (#6303)\n\nSigned-off-by: Bogdan Drutu <bogdandrutu@gmail.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-03-21T15:32:37-05:00",
          "tree_id": "9a69f45ac868a9ee2dff8323cc9c773334e8af5d",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/d8f26b221e35cb529ff48e8fd54c8bd35fd57d9f"
        },
        "date": 1711053377808,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7351.622281018773,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8216.756667714662,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12862.541997888517,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16739.11138535716,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13418.155312908008,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687097.3737987732,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8643.209234261569,
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
          "id": "434609a7bc84564504f32da2b84f0720f281422f",
          "message": "Update errorProneVersion to v2.26.1 (#6288)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-21T15:32:51-05:00",
          "tree_id": "240ad6e7cf8b45b8f655c9c9bceb732495a61d0b",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/434609a7bc84564504f32da2b84f0720f281422f"
        },
        "date": 1711053633687,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7251.83953906225,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8721.829550547534,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12996.766801073887,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18086.421912102003,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16594.801355569634,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686811.3320042181,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8539.135018671976,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "b897510bb993899fa00c4d7e537c158459ea3d6e",
          "message": "Add get{Signal}Exporter methods to Simple{Signal}Processor, Batch{Signal}Processor (#6078)",
          "timestamp": "2024-03-22T10:38:30-05:00",
          "tree_id": "4cbb004991ff0b330c87e2f83e6b3028cbc5361e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/b897510bb993899fa00c4d7e537c158459ea3d6e"
        },
        "date": 1711122146149,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7309.299659656925,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8467.139835752576,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12115.843944574373,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18855.738623670102,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16716.844153009682,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1686289.368765785,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8581.266960309302,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "13ea3346a9f1062fd3cc3963602911aca546e1d1",
          "message": "Rename EventEmitter and related classes to EventLogger (#6316)",
          "timestamp": "2024-03-26T09:31:47-05:00",
          "tree_id": "fbbe085ee90ebaa3a27b767f45de363f38461012",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/13ea3346a9f1062fd3cc3963602911aca546e1d1"
        },
        "date": 1711463748703,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7194.734066152729,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8360.236205799374,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11482.242173382188,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16673.76529204858,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16035.411827681964,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681456.025631345,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8579.383985821862,
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
          "id": "4f99e7cb4ee2e5df7db28466003415c37f533a93",
          "message": "Update dependency gradle to v8.7 (#6321)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-28T09:49:48-05:00",
          "tree_id": "d480d019f3d23519a83e5882ab8b899f2db22ae8",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/4f99e7cb4ee2e5df7db28466003415c37f533a93"
        },
        "date": 1711637722789,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7248.915158070105,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8489.348312556558,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13122.46863791203,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17434.3307250426,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 17121.082725268923,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1678703.8456579335,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8535.01393598685,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "robert.lyb@alibaba-inc.com",
            "name": "Robert Lu",
            "username": "robberphex"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "53fd6f063dba61eb3bbff8ae120d572eb8d5b561",
          "message": "migrate gradle/gradle-build-action to gradle/actions/setup-gradle (#6326)",
          "timestamp": "2024-03-28T16:36:35-05:00",
          "tree_id": "0ba80bea734f19439448fe13718b30d4f01a59d6",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/53fd6f063dba61eb3bbff8ae120d572eb8d5b561"
        },
        "date": 1711662012830,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7299.690937293745,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8790.677107711208,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 11942.207520638749,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17753.884968759587,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16816.02119910875,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691111.6584146707,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8448.584243865394,
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
          "id": "f95b9f50c16967a74da4581aa774bc813e305167",
          "message": "Update dependency com.linecorp.armeria:armeria-bom to v1.27.3 (#6325)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T10:50:22-05:00",
          "tree_id": "fdb6f90c7dd72abd5399f176e33ac5a3f6a35dbe",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/f95b9f50c16967a74da4581aa774bc813e305167"
        },
        "date": 1711727644098,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7293.002190748715,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8559.64144086829,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13475.075683582661,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 19439.359474403354,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14871.705876106851,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687453.053696844,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8637.17858473043,
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
          "id": "52bbd917f5b1cacea6a438310c6734db35282f28",
          "message": "Update dependency nl.jqno.equalsverifier:equalsverifier to v3.16 (#6320)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T10:50:38-05:00",
          "tree_id": "86df7a32eb7d48b530a46fca87c9a39362b4d8d4",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/52bbd917f5b1cacea6a438310c6734db35282f28"
        },
        "date": 1711727867204,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7352.266522758888,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8523.420167124119,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13786.99615667983,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17160.86445104183,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 19220.84097602326,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1687312.3074128453,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8613.14051861841,
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
          "id": "e688c256ac1966910a22725cf5865dcc7f84f026",
          "message": "Update dependency io.prometheus:prometheus-metrics-exporter-httpserver to v1.2.0 (#6322)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>\r\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-03-29T10:50:59-05:00",
          "tree_id": "a45d5f20c9beee2fbe6af0f50a167efc904ba239",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/e688c256ac1966910a22725cf5865dcc7f84f026"
        },
        "date": 1711728161345,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7181.6034523147855,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8231.648064172114,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12390.14843910323,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18303.089493578314,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13599.9591248337,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682501.586432042,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8551.169370927875,
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
          "id": "101c0a80f2e6c605d1d8844fe2ad5b3d08a33970",
          "message": "Update gradle/wrapper-validation-action action to v2.1.2 (#6319)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T10:51:10-05:00",
          "tree_id": "29eeadcd92331d6231cc867ec4ffcd010c767539",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/101c0a80f2e6c605d1d8844fe2ad5b3d08a33970"
        },
        "date": 1711728379236,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7296.488944813728,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8739.188316533153,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12537.623342788323,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17700.967614572226,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 13808.85872273313,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1691194.3868367355,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8621.368283568496,
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
          "id": "80971c1ff7d9dacd4ca5009e69637697c0dcecfd",
          "message": "Update dependency io.netty:netty-bom to v4.1.108.Final (#6315)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T10:51:24-05:00",
          "tree_id": "cbb6a2957398365b1e3868479b1eb0365d2a2139",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/80971c1ff7d9dacd4ca5009e69637697c0dcecfd"
        },
        "date": 1711728616518,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7276.569178093018,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8663.16475836505,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13493.186828208189,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18160.040171569963,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16425.664297082512,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1684827.5821889949,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8596.754321067385,
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
          "id": "570d5827306f8de3804e39a529369453a3496235",
          "message": "Update dependency com.uber.nullaway:nullaway to v0.10.25 (#6330)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T15:51:38-05:00",
          "tree_id": "5a8afafc1048ba006cfb324144fb163ada59c36e",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/570d5827306f8de3804e39a529369453a3496235"
        },
        "date": 1711745749992,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7210.94571036781,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8360.816988062981,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13581.773769193815,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16133.453432182694,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16805.054576556748,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1677079.4674430331,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8582.929001465074,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "622d977f8726bd2eb50b8bb66b171b1cf8883e25",
          "message": "Refactor Event API to reflect spec changes (#6318)",
          "timestamp": "2024-03-29T16:48:12-05:00",
          "tree_id": "62625ba68241220bbf53204ee29a224c4038e1e7",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/622d977f8726bd2eb50b8bb66b171b1cf8883e25"
        },
        "date": 1711749133586,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7238.971595776633,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8531.373764138787,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 10503.866339074422,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17305.274809661736,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 15227.868140421575,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1681853.646996937,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8616.63776936234,
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
          "id": "98eded9f862e1c404d761fd680cfb0c51acade34",
          "message": "Update dependency com.squareup.wire:wire-bom to v4.9.8 (#6302)\n\nCo-authored-by: renovate[bot] <29139614+renovate[bot]@users.noreply.github.com>",
          "timestamp": "2024-03-29T16:48:29-05:00",
          "tree_id": "3ba445024a8d2861f121e0ead1f16236916d6236",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/98eded9f862e1c404d761fd680cfb0c51acade34"
        },
        "date": 1711749428497,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7195.548282698314,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8652.855700790027,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13382.70909204384,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18432.21378924413,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14667.667966156834,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1682222.6357412557,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8565.659053578274,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "gregor.zeitlinger@grafana.com",
            "name": "Gregor Zeitlinger",
            "username": "zeitlinger"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "ec464079ee611c2fe8830c338b7dcac57161c803",
          "message": "service.instance.id implementation (#6226)\n\nCo-authored-by: Jack Berg <jberg@newrelic.com>",
          "timestamp": "2024-04-02T11:47:00-05:00",
          "tree_id": "11bea049587da1b68291b74d7e2be520f002d663",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/ec464079ee611c2fe8830c338b7dcac57161c803"
        },
        "date": 1712076638798,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7371.666001436461,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8441.66732692951,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12937.374884129342,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 15980.613893780468,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 16306.159606443704,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690233.9631062646,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8619.93232235651,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "sharmapranav@google.com",
            "name": "Pranav Sharma",
            "username": "psx95"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "40d18f614a58020918a961ffdca401ca73ef4602",
          "message": "Add GCP resource detector to ResourceProvider list (#6336)",
          "timestamp": "2024-04-02T11:53:42-05:00",
          "tree_id": "a29dec6b93c23785c35df04a49b13642501d460c",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/40d18f614a58020918a961ffdca401ca73ef4602"
        },
        "date": 1712077039447,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7421.8292108313435,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8603.860362273172,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 12876.167989219317,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17123.199720871216,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14391.845439960909,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690732.1927798167,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8620.827473236175,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "asaf.mesika@gmail.com",
            "name": "Asaf Mesika",
            "username": "asafm"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "bc5eb9ea24b32f7ce906fcf46e3e321e6f8455e2",
          "message": "Add toBuilder() at PrometheusHttpServer (#6333)\n\nCo-authored-by: jack-berg <34418638+jack-berg@users.noreply.github.com>",
          "timestamp": "2024-04-03T09:24:04-05:00",
          "tree_id": "4174c8334a5f84aa8d9fb76474a80d4a97382944",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/bc5eb9ea24b32f7ce906fcf46e3e321e6f8455e2"
        },
        "date": 1712154705744,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7280.516204850386,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8285.532729403474,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13319.008141315764,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 17806.88174401889,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14451.41280632423,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690264.7298984516,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8598.59334438496,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "1dde2550b01fd5fa4582c8ac5ab74c34a445ff5f",
          "message": "Use synchronized instead of reentrant lock in explicit bucket histogram (#6309)",
          "timestamp": "2024-04-03T10:23:17-05:00",
          "tree_id": "80588f45e10f18fdcae577e85b3316626c2762be",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/1dde2550b01fd5fa4582c8ac5ab74c34a445ff5f"
        },
        "date": 1712158021524,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7299.562498309246,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8449.43366511918,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 14259.568439233797,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 18678.413613987614,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14077.747716828362,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1690629.6543537166,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8582.706328253485,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      },
      {
        "commit": {
          "author": {
            "email": "34418638+jack-berg@users.noreply.github.com",
            "name": "jack-berg",
            "username": "jack-berg"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "6c11235f6f30eb5313fe66d58026a985ed834f12",
          "message": "Restore build fail on warning (#6347)",
          "timestamp": "2024-04-04T09:42:37-05:00",
          "tree_id": "f3a9f9cbfbb9ec16797d9e1c7cec7352f435de12",
          "url": "https://github.com/open-telemetry/opentelemetry-java/commit/6c11235f6f30eb5313fe66d58026a985ed834f12"
        },
        "date": 1712242050997,
        "tool": "jmh",
        "benches": [
          {
            "name": "io.opentelemetry.sdk.trace.FillSpanBenchmark.setFourAttributes",
            "value": 7289.136839955996,
            "unit": "ops/ms",
            "extra": "iterations: 20\nforks: 3\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_01Thread",
            "value": 8278.759507014143,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_02Threads",
            "value": 13040.599775311726,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 2"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_05Threads",
            "value": 16795.814018941008,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 5"
          },
          {
            "name": "io.opentelemetry.sdk.trace.SpanBenchmark.simpleSpanStartAddEventEnd_10Threads",
            "value": 14603.12309692839,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 10"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"1\",\"spanCount\":\"1000\"} )",
            "value": 1683387.5207222104,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          },
          {
            "name": "io.opentelemetry.sdk.trace.export.MultiSpanExporterBenchmark.export ( {\"exporterCount\":\"3\",\"spanCount\":\"1000\"} )",
            "value": 8537.522129210305,
            "unit": "ops/ms",
            "extra": "iterations: 10\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}