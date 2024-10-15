FROM openjdk:17
WORKDIR /app
COPY target/*.jar app.jar

ENV DATABASE_URL ${DATABASE_URL}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]