#!/bin/bash
# Writes the PostgreSQL image version that Artemis develop uses into docker/.env, where Docker Compose reads it
# for the E2E stack. Only these two lines are copied: the rest of the Artemis .env, COMPOSE_PROJECT_NAME in
# particular, would change how Compose names and cleans up this stack.
set -euo pipefail

target="$(dirname "$0")/.env"
versions="$(curl -fsSL https://raw.githubusercontent.com/ls1intum/Artemis/develop/.env | grep -E '^POSTGRES_(VERSION|IMAGE_VARIANT)=')"

if [ "$(printf '%s\n' "$versions" | wc -l)" -ne 2 ]; then
    echo "The Artemis .env no longer defines POSTGRES_VERSION and POSTGRES_IMAGE_VARIANT" >&2
    exit 1
fi

printf '%s\n' "$versions" > "$target"
cat "$target"
