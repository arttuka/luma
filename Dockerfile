FROM openjdk:8-alpine AS builder
ENV LEIN_ROOT true
RUN apk --no-cache add curl bash nodejs nodejs-npm chromium
RUN \
    # Create chromium wrapper with required flags
    mv /usr/bin/chromium-browser /usr/bin/chromium-browser-origin && \
    echo $'#!/usr/bin/env sh\n\
    chromium-browser-origin --no-sandbox --headless --disable-gpu $@' > /usr/bin/chromium-browser && \
    chmod +x /usr/bin/chromium-browser

ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROME_PATH=/usr/lib/chromium/
RUN    npm install --silent --global karma-cli \
    && npm install --silent --save-dev phantomjs-prebuilt karma karma-cljs-test karma-junit-reporter karma-chrome-launcher

RUN curl -Lo /usr/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && chmod +x /usr/bin/lein
RUN lein
WORKDIR /app
COPY project.clj .
RUN lein deps
COPY . .
RUN lein test
RUN lein doo chrome-headless test once
RUN lein do clean, uberjar

FROM openjdk:8-alpine
WORKDIR /app
COPY --from=builder /app/target/luma.jar .
CMD ["java", "-jar", "luma.jar"]
