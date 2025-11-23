FROM openjdk:25-rc-jdk AS build
WORKDIR /app
COPY mvnw* .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -Dspring.profiles.active=prod

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/RevGif-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]