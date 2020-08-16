# build project
FROM maven:latest AS build
WORKDIR /usr/src/app
COPY pom.xml .
# cache most of the dependecies as long as pom doesn't change
RUN mvn dependency:go-offline

COPY src /usr/src/app/src/
RUN mvn package

########################################################################################################################

# Move jar from build to final image and execute it
FROM openjdk:13-alpine
COPY --from=build "/usr/src/app/target/ArgsSearch-1.0-shaded.jar" /usr/src/app/app.jar
WORKDIR /usr/src/app/

# On run execute the jar and hand over the database url as parameter
CMD ["java", "-jar", "app.jar"]