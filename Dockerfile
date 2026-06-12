FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/target/audit-service-*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
