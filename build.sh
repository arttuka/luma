#!/bin/bash
set -euo pipefail

version=$(git log --pretty=format:'%h' -n 1)
tag=arttuka/luma:$version

docker build . -t $tag
docker tag $tag arttuka/luma:latest