FROM maven:3.9-amazoncorretto-21 AS BUILD
WORKDIR /app/

COPY pom.xml .
COPY src src
RUN mvn install -DskipTests

FROM amazoncorretto:21.0.0 AS RUNTIME

WORKDIR /app/
COPY --from=BUILD /app/target/*.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -jar /app/app.jar
