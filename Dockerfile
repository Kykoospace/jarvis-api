FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
ARG APP_PATH=/srv/jarvis-api
COPY ${JAR_FILE} ${APP_PATH}/app.jar
ENTRYPOINT ["java","-jar","${APP_PATH}/app.jar"]
