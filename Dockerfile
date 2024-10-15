FROM openjdk:17
WORKDIR /app
COPY target/*.jar app.jar

ENV DATABASE_URL ${DATABASE_URL}

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]