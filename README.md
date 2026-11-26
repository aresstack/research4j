# Research4j

Reusable Java research and browser components extracted from [Ask AI](https://github.com/aresstack/askai) at commit [3dfeecee60b1ee7db9e7cbead98cef13240ff0b6](https://github.com/aresstack/askai/commit/3dfeecee60b1ee7db9e7cbead98cef13240ff0b6). Ask AI remains unchanged; migration is tracked in [Ask AI #292](https://github.com/aresstack/askai/issues/292).

## Modules

- browser-api, browser-playwright, browser-search-analysis, browser-static-http
- web-search-providers, serp4j-playwright-google
- research-domain, research-agent-runtime
- agent-model-runtime-api, mcp-runtime-api (neutral host contracts)

The original Java packages and resources are retained to minimize migration changes. Java 21 matches the upstream build. The Playwright backend uses playwright4j and requires isolation from older GraalJS versions in host applications.

## Build

Publish Serp4j to Maven Local first (upstream currently consumes 0.1.0-SNAPSHOT). Then run:

```sh
bash gradlew clean build
bash gradlew publishToMavenLocal
```

Ask AI host-dependent tests remain in the source tree but are explicitly excluded from the standalone test source set until replaced with host-neutral fixtures. The existing Firefox channel is retained; persistent Firefox profiles, extension loading, intranet access policy and Space Harvester integration require separate validation. No Maven Central release is claimed.

## License

MIT; see LICENSE.
