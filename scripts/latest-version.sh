#!/usr/bin/env bash

tags=$(git tag --sort=-version:refname)
latest_tag=$(echo "$tags" | head -1)
echo "$latest_tag"
