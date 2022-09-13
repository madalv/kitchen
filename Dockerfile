
FROM openjdk:18-slim
#FROM gradle:7-jdk18-alpine AS build
WORKDIR /src
COPY . /src

#RUN gradle buildFatJar


EXPOSE 8082:8082
RUN mkdir /app
RUN cp /src/build/libs/*.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]