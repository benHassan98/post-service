FROM eclipse-temurin:17-jdk-alpine

#you need to package the app first
COPY ./target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
