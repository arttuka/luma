#!/usr/bin/env bash
set -euxo pipefail

lein clean
lein test
npm test
lein kibit
lein eastwood
lein cljfmt check
lein clean
npm run build
lein do \
  buster, \
  uberjar

version=$(git log --pretty=format:'%h' -n 1)
tag=arttuka/luma:$version

docker build . -t $tag --pull
docker tag $tag arttuka/luma:latest
