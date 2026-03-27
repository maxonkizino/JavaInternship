# Этап сборки с Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем файлы проекта
COPY pom.xml .
COPY src ./src

# Собираем проект (используем mvn, так как он уже установлен в образе)
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем собранный jar из этапа сборки
COPY --from=build /app/target/JavaInternship-0.0.1-SNAPSHOT.jar app.jar

# Устанавливаем профиль для Docker
ENV SPRING_PROFILES_ACTIVE=docker

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]