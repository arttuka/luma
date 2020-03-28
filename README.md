# LUMA Ultimate Music Archive

A web service that presents the user a sortable and filterable view of their Spotify library.

[https://luma.dy.fi](https://luma.dy.fi)

## Development Mode

Clone the `reagent-util` submodule first:
```
git submodule init
git submodule update
```

Start a REPL, then run `(user/start!)` to run shadow-cljs and web server with automatic reload of all resources. The server runs at http://localhost:8080

### Run Clojure tests:

```
lein test
```

### Run ClojureScript tests

```
npm test
```

## Production Build

```
./build.sh
```

