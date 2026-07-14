# Используем образ с Java 17
FROM openjdk:17-jdk-slim

# Копируем скомпилированный jar-файл из папки target
COPY target/*.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app.jar"]