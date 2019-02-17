FROM openjdk:8-alpine AS builder
ENV LEIN_ROOT true
RUN apk --no-cache add curl bash chromium
RUN \
    # Create chromium wrapper with required flags
    mv /usr/bin/chromium-browser /usr/bin/chromium-browser-origin && \
    echo $'#!/usr/bin/env sh\n\
    chromium-browser-origin --no-sandbox --headless --disable-gpu --repl $@' > /usr/bin/chromium-browser && \
    chmod +x /usr/bin/chromium-browser

RUN curl -Lo /usr/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && chmod +x /usr/bin/lein
RUN lein
WORKDIR /app
COPY project.clj .
RUN lein deps
COPY ./src ./src
COPY ./test ./test
COPY ./resources ./resources
COPY ./*.cljs.edn ./
RUN lein test
RUN lein fig:test
RUN lein do clean, uberjar

FROM openjdk:8-alpine
WORKDIR /app
COPY --from=builder /app/target/luma.jar .
CMD ["java", "-jar", "luma.jar"]
