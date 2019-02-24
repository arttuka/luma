FROM openjdk:11-slim AS builder
ENV LEIN_ROOT true
RUN apt-get update && apt-get install -y curl chromium
RUN \
    # Create chromium wrapper with required flags
    mv /usr/bin/chromium /usr/bin/chromium-origin && \
    echo $'#!/usr/bin/env sh\n\
    chromium-origin --no-sandbox --headless --disable-gpu --repl $@' > /usr/bin/chromium-browser && \
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

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /app/target/luma.jar .
CMD ["java", "-jar", "luma.jar"]
