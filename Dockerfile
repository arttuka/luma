FROM adoptopenjdk/openjdk12-openj9:alpine AS builder
ENV LEIN_ROOT true
RUN apk --no-cache add bash curl chromium nodejs npm \
    # Create chromium wrapper with required flags
    && mv /usr/bin/chromium-browser /usr/bin/chromium-browser-origin \
    && echo $'#!/usr/bin/env sh\n\
      chromium-browser-origin --no-sandbox --headless --disable-gpu --repl $@' > /usr/bin/chromium-browser \
    && chmod +x /usr/bin/chromium-browser \
    && curl -Lo /usr/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
    && chmod +x /usr/bin/lein
WORKDIR /app
COPY project.clj .
RUN lein deps
COPY ./src ./src
COPY ./test ./test
COPY ./resources ./resources
COPY ./test-resources ./test-resources
COPY ./*.cljs.edn package.json package-lock.json webpack.config.js ./
RUN npm i \
    && npm run build:dev \
    && npm run build:prod \
    && lein do \
         test, \
         fig:test, \
         kibit, \
         eastwood, \
         cljfmt check \
    && lein with-profile provided do \
         clean, \
         garden once, \
         minify-assets, \
         fig:min, \
         buster, \
         uberjar

FROM adoptopenjdk/openjdk12-openj9:alpine-jre
WORKDIR /app
COPY --from=builder /app/target/luma.jar .
CMD ["java", "-jar", "luma.jar"]
