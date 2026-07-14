# Используем образ с Java 17
FROM eclipse-temurin:17-jdk-jammy

# Копируем скомпилированный jar-файл из папки target
COPY target/*.jar app.jar

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app.jar"]