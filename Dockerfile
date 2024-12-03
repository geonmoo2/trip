# Java 21 기반 Eclipse Temurin 이미지로 빌드
FROM eclipse-temurin:21-jdk AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle /app/
COPY settings.gradle /app/

# Ensure the Gradle wrapper script has Unix line endings and is executable
RUN apt-get update && apt-get install -y dos2unix \
    && dos2unix /app/gradlew \
    && chmod +x /app/gradlew \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Download dependencies (using --no-daemon to prevent issues in CI/CD environments)
RUN ./gradlew --no-daemon dependencies

# Copy the source code
COPY src /app/src

# Clean and build the project (excluding tests)
RUN ./gradlew clean build -x test --no-daemon --stacktrace --info

# Step 2: Runtime Stage
FROM eclipse-temurin:21-jdk AS runtime

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/*.jar /app/sl.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/sl.jar"]
