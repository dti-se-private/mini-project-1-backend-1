# Set builder image.
FROM gradle:jdk21-graal

# Set working directory.
ENV WORKDIR=/workdir
WORKDIR $WORKDIR

# Copy source code.
COPY . .
RUN rm .env

# Build the application.
RUN --mount=type=cache,target=~/.gradle,sharing=locked \
    gradle bootJar

# Copy built artifacts.
RUN cp build/libs/*.jar app.jar

# Run the application.
CMD ["java", "-jar", "app.jar"]


