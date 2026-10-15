# ===== Build stage: compiles the app with Maven =====
# Use an image with Maven 3.9.9 and JDK 21; name this stage "build" so we can copy from it later
FROM maven:3.9.9-eclipse-temurin-21 AS build
# Set /app as the working directory for all following commands
WORKDIR /app
# Copy only pom.xml first so the dependency download below can be cached
COPY pom.xml .
# Download all dependencies in batch mode (cached as long as pom.xml doesn't change)
RUN mvn dependency:go-offline -B
# Copy the source code (changes here don't invalidate the cached dependencies)
COPY src ./src
# Compile and package the app into a JAR in /app/target, skipping tests for a faster build
RUN mvn package -DskipTests


# ===== Run stage: small image that only runs the built JAR =====
# Start fresh from a lightweight Java 21 runtime (JRE only, no Maven or compiler)
FROM eclipse-temurin:21-jre
# Set /app as the working directory in this new image
WORKDIR /app
# Copy the JAR built in the "build" stage and rename it to app.jar
COPY --from=build /app/target/*.jar app.jar
# Document that the app listens on port 8761 (Eureka's default); doesn't publish it by itself
EXPOSE 8081
# Start the app when the container runs: java -jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]