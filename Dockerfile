# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM kyle11235/akcs-api-builder as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn package -DskipTests

FROM docker:dind

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/*.jar /app.jar

# Run the web service on container startup.
CMD ["docker","run","hello-world"]