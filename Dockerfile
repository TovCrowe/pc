# syntax=docker/dockerfile:1

# ---- Build stage ----
# Compiles the app and produces the runnable JAR. Uses a full JDK + Maven.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the POM first and pre-download dependencies — this layer is cached
# as long as pom.xml doesn't change, so rebuilds are fast.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the source and build. Tests are skipped here (run them in CI / locally
# with ./mvnw test) so the image build stays fast.
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Run stage ----
# A slim JRE-only image — no Maven, no source — for a smaller, safer container.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user for safety.
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Documentation only — the actual port is set by the PORT env var at runtime.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
