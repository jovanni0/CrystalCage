FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/app-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]