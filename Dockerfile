FROM openjdk:8-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
COPY ./target/*.jar /srv/jarvis-api/app.jar
ENTRYPOINT ["java","-jar","/srv/jarvis-api/app.jar"]
