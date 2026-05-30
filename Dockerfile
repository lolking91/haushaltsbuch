# =============================================================================
# Multi-stage Dockerfile for Haushaltsbuch
#
# Stage 1 (frontend-build): Builds the SvelteKit frontend with Node.js.
#                           The adapter-static outputs directly into the
#                           backend's static resources directory.
# Stage 2 (backend-build):  Builds the Spring Boot JAR with Maven, which
#                           embeds the frontend output as static resources.
# Stage 3 (runtime):        Minimal JRE image that runs the JAR.
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Frontend Build
# -----------------------------------------------------------------------------
FROM --platform=$BUILDPLATFORM node:22-alpine AS frontend-build

WORKDIR /app/frontend

# Copy dependency manifests first for better layer caching
COPY frontend/package*.json ./
RUN npm ci

# Copy frontend source
COPY frontend/ ./

# Create the output directory expected by adapter-static
# (resolves to ../backend/src/main/resources/static relative to WORKDIR)
RUN mkdir -p /app/backend/src/main/resources/static

# Build SvelteKit → outputs to /app/backend/src/main/resources/static
RUN npm run build

# -----------------------------------------------------------------------------
# Stage 2: Backend Build
# -----------------------------------------------------------------------------
FROM --platform=$BUILDPLATFORM maven:3-eclipse-temurin-21 AS backend-build

WORKDIR /app/backend

# Copy POM first for dependency caching
COPY backend/pom.xml ./

# Download all dependencies (cached as long as pom.xml doesn't change)
RUN mvn dependency:go-offline -q

# Copy backend source
COPY backend/src/ ./src/

# Overlay the built frontend static files from Stage 1
COPY --from=frontend-build /app/backend/src/main/resources/static \
     ./src/main/resources/static

# Build the fat JAR, skip tests (tests run in CI separately)
RUN mvn package -DskipTests -q

# -----------------------------------------------------------------------------
# Stage 3: Runtime
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# Run as non-root user for security
RUN addgroup -S haushaltsbuch && adduser -S haushaltsbuch -G haushaltsbuch

WORKDIR /app

COPY --from=backend-build /app/backend/target/*.jar app.jar

RUN chown haushaltsbuch:haushaltsbuch app.jar

USER haushaltsbuch

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
