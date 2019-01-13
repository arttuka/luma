# LUMA Ultimate Music Archive

A web service that presents the user a sortable and filterable view of their Spotify library.

## Development Mode

Start a REPL, then run `(user/start!)` to run figwheel, garden and server with automatic reload of all resources.

### Run Clojure tests:

```
lein test
```

### Run ClojureScript tests

When you start figwheel in the REPL, it opens an interactive test runner in a browser.

## Production Build

```
docker build . -t arttuka/luma:latest
```

