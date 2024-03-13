FROM amazoncorretto:21.0.2-alpine3.19
ADD /target/server-0.0.1-SNAPSHOT.jar backend.jar
ENTRYPOINT ["java", "-jar", "backend.jar"]