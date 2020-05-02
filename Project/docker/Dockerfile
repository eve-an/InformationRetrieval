FROM maven:3.6-jdk-13-alpine AS target

WORKDIR /build
COPY pom.xml .

# Cache dependencies as long as they don't change
# Dependencies don't change that much so we save a little time
RUN mvn dependency:go-offline 

# Copy sources and build them
COPY src/ /build/src/
RUN mvn clean package


FROM openjdk:13-alpine
CMD exec java $JAVA_OPTS -jar /app/my-app.jar
COPY --from=target /build/target/*spring-boot.jar /app/my-app.jar