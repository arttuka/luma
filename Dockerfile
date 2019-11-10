FROM adoptopenjdk/openjdk12-openj9:alpine-jre
WORKDIR /app
COPY ./target/luma.jar .
CMD ["java", "-jar", "luma.jar"]
