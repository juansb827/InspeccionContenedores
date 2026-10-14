#!/usr/bin/env bash
set -euo pipefail
repo_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"
tasks=(:app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug)
options=()
focused=false
fresh=false
while (($#)); do
  case "$1" in
    --fresh) options+=(--rerun-tasks); fresh=true; shift ;;
    --tests)
      if (($# < 2)); then echo '--tests requires a JUnit pattern' >&2; exit 2; fi
      focused=true; options+=(--tests "$2"); shift 2 ;;
    *) echo 'Usage: scripts/check.sh [--fresh] [--tests PATTERN ...]' >&2; exit 2 ;;
  esac
done
if $focused; then tasks=(:app:testDebugUnitTest); fi
if [[ -z "${JAVA_HOME:-}" ]] && ! command -v java >/dev/null; then
  echo 'Configure JAVA_HOME to a compatible JDK (21 verified) and a local Android SDK.' >&2
  exit 2
fi
printf 'JVM/build checks only; fresh execution requested: %s\n' "$fresh"
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' \
  --max-workers=2 "${tasks[@]}" "${options[@]}"
python3 scripts/test-report.py
