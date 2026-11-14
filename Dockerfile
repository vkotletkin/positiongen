# ===== build =====
FROM maven:3.9.15-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests \
    && find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' \
         -exec cp {} /app/application.jar \;

# ===== runtime (rootless) =====
FROM eclipse-temurin:25.0.3_9-jre-alpine-3.22

RUN addgroup -S -g 10001 app \
    && adduser -S -u 10001 -G app -H -D app \
    && mkdir -p /app \
    && chown -R app:app /app

WORKDIR /app

COPY --from=build --chown=app:app /app/application.jar app.jar

USER 10001:10001

ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/tmp"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
