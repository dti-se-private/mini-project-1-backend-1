# Set builder image.
FROM gradle:jdk21-graal

# Set working directory.
ENV WORKDIR=/workdir
WORKDIR $WORKDIR

# Copy source code.
COPY . .

# Build the application.
RUN --mount=type=cache,target=~/.gradle,sharing=locked \
    gradle build -x test

# Copy built artifacts.
COPY build/libs/*.jar app.jar

# Run the application.
CMD ["java", "-jar", "app.jar"]


