#!/usr/bin/env bash
set -euxo pipefail

lein clean
lein test
lein fig:test
lein kibit
lein eastwood
lein cljfmt check
lein with-profile provided do \
  clean, \
  fig:min, \
  buster, \
  uberjar

version=$(git log --pretty=format:'%h' -n 1)
tag=arttuka/luma:$version

docker build . -t $tag --pull
docker tag $tag arttuka/luma:latest
