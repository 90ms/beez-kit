---
name: verify-android-library
description: Review and verify a BeezKit module or release candidate for public API simplicity, lifecycle and memory safety, concurrency, Compose behavior, accessibility, dependency boundaries, documentation, tests, R8 readiness, and build health. Use for readiness checks, regression reviews, or pre-release audits; do not implement fixes unless explicitly requested.
---

# Verify an Android library

1. Read repository `AGENTS.md`, `docs/architecture.md`, `docs/sample-catalog.md`, the affected module specifications, and [verification checklist](references/verification-checklist.md).
2. Inspect public source, Gradle dependencies, manifests, consumer rules, tests, and catalog usage. Compare implemented behavior to the canonical spec.
3. Exercise the one-line happy path and advanced path from a host-app perspective. Flag ambiguous ownership, required boilerplate, leaked third-party types, and naming violations.
4. Audit lifecycle, retained references, coroutine cancellation, concurrency, bounds, sensitive-data handling, accessibility, recomposition, and debug/release separation.
5. Run local static checks only. Do not run Gradle locally. Inspect the GitHub Actions Android validation result when a pushed branch or pull request exists; otherwise report Gradle verification as pending.
6. Report findings by severity with file evidence, observable impact, and a safe direction. Distinguish confirmed defects from missing verification.
7. Do not change implementation during a review-only request. Do not mark a module Stable unless every release gate has evidence.
