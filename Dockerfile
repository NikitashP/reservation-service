
FROM openjdk:8-jdk-alpine

VOLUME /tmp

EXPOSE 8086

ARG JAR_FILE=target/reservation-0.0.1-SNAPSHOT.jar


ADD ${JAR_FILE} reservation-service.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/reservation-service.jar","--host=dockerhost"]
