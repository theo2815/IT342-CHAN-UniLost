# syntax=docker/dockerfile:1.6

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src src
RUN ./mvnw -B -ntp clean package -DskipTests \
 && cp target/*.jar /workspace/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as non-root for container security. -XX:MaxRAMPercentage=75 keeps the JVM
# inside Render's free-plan 512MB limit instead of grabbing 25% of the host.
RUN groupadd -r app && useradd -r -g app -d /app -s /sbin/nologin app \
 && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
