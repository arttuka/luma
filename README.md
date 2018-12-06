# LUMA Ultimate Music Archive

A web service that presents the user a sortable and filterable view of their Spotify library.

## Development Mode

Start a REPL, then run `(user/start!)` to run figwheel, garden and server with automatic reload of all resources.

### Run Clojure tests:

```
lein test
```

### Run ClojureScript tests

`karma` needs to be installed:
```
npm install -g karma karma-cli karma-cljs-test karma-junit-reporter karma-chrome-launcher
```

Then you can run tests:
```
lein doo chrome-headless test
```

The above command assumes that you have [phantomjs](https://www.npmjs.com/package/phantomjs) installed. However, please note that [doo](https://github.com/bensu/doo) can be configured to run cljs.test in many other JS environments (chrome, ie, safari, opera, slimer, node, rhino, or nashorn).

## Production Build

```
docker build . -t arttuka/luma:latest
```

