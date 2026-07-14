FROM maven3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin17-jre-alpine
COPY --from=build target.jar app.jar
EXPOSE 8080
ENTRYPOINT [java, -jar, app.jar]