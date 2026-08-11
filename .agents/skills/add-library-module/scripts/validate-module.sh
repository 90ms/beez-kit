#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: validate-module.sh <gradle-module-path> <spec-path>" >&2
    exit 2
fi

module_path="${1#:}"
module_dir="${module_path//:/\/}"
spec_path="$2"
catalog_registry="samples/catalog/src/main/kotlin/io/github/beez/beezkit/samples/catalog/CatalogRegistry.kt"
catalog_build="samples/catalog/build.gradle.kts"

test -f "settings.gradle.kts"
test -f "$module_dir/build.gradle.kts"
test -f "$spec_path"
test -f "$catalog_registry"
test -f "$catalog_build"
grep -Fq "\":${module_path}\"" settings.gradle.kts
grep -Fq "${spec_path}" README.md
grep -Fq "modulePath = \":${module_path}\"" "$catalog_registry"
grep -Fq "project(\":${module_path}\")" "$catalog_build"

echo "Module, documentation, and catalog wiring look complete: :${module_path}"
