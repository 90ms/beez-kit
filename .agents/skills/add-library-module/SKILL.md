---
name: add-library-module
description: Add a new Gradle module to the BeezKit repository, including settings registration, convention plugins, namespace and artifact naming, canonical specification, README inventory link, tests, and catalog wiring. Use only when a new module or adapter artifact is required.
---

# Add a library module

1. Read repository `AGENTS.md`, `docs/architecture.md`, and [module checklist](references/module-checklist.md).
2. Confirm the feature deserves a module because it has a cohesive public API, optional dependency, resource boundary, debug/release boundary, or independent artifact value.
3. Choose `toolkit`, `components`, or an adapter beneath an existing module. Use lowercase Gradle paths and artifact IDs.
4. Register the module in `settings.gradle.kts`, apply repository convention plugins, and assign a unique namespace under `io.github.beez.beezkit`.
5. Add only necessary dependencies. Use `api` solely when a dependency type is part of the public contract.
6. Create `docs/modules/<name>.md` with goal, coordinates, status, target API, required behavior, safety constraints, and verification criteria.
7. Add the artifact and relative spec link to the root README. Wire a catalog example when the feature is visual.
8. Add baseline tests and run `scripts/validate-module.sh <module-path> <spec-path>` from the repository root.
9. Run module build/lint and repository build when the Android SDK is available.

Do not implement speculative features merely to fill the scaffold. Keep status Planned until behavior and tests exist.

