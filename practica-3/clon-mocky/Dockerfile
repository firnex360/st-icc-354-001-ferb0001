# ==============================
# Stage 1: Build con Gradle + JDK 25
# ==============================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiar archivos del Gradle Wrapper primero 
COPY gradlew .
COPY gradle/ gradle/
RUN chmod +x gradlew

# Copiar archivos de configuración de Gradle
COPY build.gradle .
COPY settings.gradle .

# Descargar dependencias 
RUN ./gradlew dependencies --no-daemon || true

# Copiar el código fuente
COPY src/ src/

# Compilar el fat JAR
RUN ./gradlew bootJar --no-daemon -x test

# ==============================
# Stage 2: Runtime con JRE 25
# ==============================
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

# Copiar el JAR generado desde el stage de build
COPY --from=build /app/build/libs/*.jar app.jar

# Puerto por defecto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
