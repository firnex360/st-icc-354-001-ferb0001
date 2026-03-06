# ====== Etapa 1: Build ======
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar archivos de Gradle wrapper y configuración primero (mejor caché de capas)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Descargar dependencias (se cachea si build.gradle no cambia)
RUN ./gradlew dependencies --no-daemon || true

# Copiar código fuente y compilar
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ====== Etapa 2: Runtime ======
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
