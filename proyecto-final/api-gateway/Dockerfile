# ==============================
# Stage 1: Build con Maven + JDK 21
# ==============================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar configuración de Maven
COPY pom.xml .

# Descargar dependencias
RUN mvn dependency:go-offline -B || true

# Copiar el código fuente
COPY src/ src/

# Compilar el fat JAR
RUN mvn clean package -DskipTests

# ==============================
# Stage 2: Runtime con JRE 21
# ==============================
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

# Copiar el JAR generado desde el stage de build
COPY --from=build /app/target/*.jar app.jar

# Puerto por defecto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
