# Frontend build
FROM node:22-alpine AS frontend-builder

WORKDIR /app/frontend

RUN corepack enable

COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY frontend/ ./
RUN pnpm build

# Backend build
FROM maven:3.9-eclipse-temurin-25 AS backend-builder

WORKDIR /app/backend

COPY backend/pom.xml ./
COPY backend/mvnw ./
COPY backend/.mvn ./.mvn
COPY backend/src ./src
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN mvn -DskipTests package

# Runtime image
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=backend-builder /app/backend/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=render

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]