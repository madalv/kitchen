
FROM openjdk:18-slim

WORKDIR /src
COPY . /src
# RUN bash gradlew buildFatJar

EXPOSE 8082:8082
RUN mkdir /app
RUN cp /src/build/libs/*.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]