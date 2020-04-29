#!/usr/bin/env bash

cd "$(dirname "$0")" || exit
ls
pwd
latest_tag=$(./latest-version.sh)
new_tag=$(./semver.sh bump "$1" "$latest_tag")
echo "$latest_tag -> $new_tag"
git tag "$new_tag"
