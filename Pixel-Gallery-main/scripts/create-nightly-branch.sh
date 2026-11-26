#!/usr/bin/env bash
# Crea (o cambia a) la rama "nightly" y la sube a GitHub. Úsalo desde Termux, en la raíz del repo:
#   bash scripts/create-nightly-branch.sh
set -e
if git show-ref --verify --quiet refs/heads/nightly; then
  git checkout nightly
else
  git checkout -b nightly
fi
git push -u origin nightly
echo "Rama nightly lista. Cada push a ella compila y publica la pre-release nightly-latest."
