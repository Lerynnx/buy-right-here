FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
