# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Cài đặt PostgreSQL client và Microsoft Core Fonts (Times New Roman)
RUN apt-get update && \
    apt-get install -y --no-install-recommends postgresql-client fontconfig && \
    echo "ttf-mscorefonts-installer msttcorefonts/accepted-mscorefonts-eula select true" | debconf-set-selections && \
    apt-get install -y --no-install-recommends ttf-mscorefonts-installer && \
    fc-cache -f -v && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/target/classes/keys /app/keys
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
