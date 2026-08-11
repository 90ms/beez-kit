#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
    echo "usage: validate-module.sh <gradle-module-path> <spec-path>" >&2
    exit 2
fi

module_path="${1#:}"
module_dir="${module_path//:/\/}"
spec_path="$2"

test -f "settings.gradle.kts"
test -f "$module_dir/build.gradle.kts"
test -f "$spec_path"
grep -Fq "\":${module_path}\"" settings.gradle.kts
grep -Fq "${spec_path}" README.md

echo "Module wiring looks complete: :${module_path}"

