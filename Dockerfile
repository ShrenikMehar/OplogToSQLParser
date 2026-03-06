FROM eclipse-temurin:25-jre

WORKDIR /app

COPY build/libs/*-all.jar app.jar

CMD ["java", "-jar", "app.jar"]