# syntax=docker/dockerfile:1

# Etapa de build con Maven y JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia solo el pom y baja dependencias (mejora el cache)
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copia el código y construye
COPY src ./src
RUN mvn -B -DskipTests package

# Etapa runtime con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# ✅ Instalar curl para health checks
RUN apk add --no-cache curl

# Copia el jar construido (toma el único .jar del target)
COPY --from=build /app/target/*.jar /app/app.jar

# ✅ Exponer puerto
EXPOSE 8080

# ✅ Variables de entorno optimizadas para Cloud Run
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication" \
    PORT=8080

# ✅ Health check para Cloud Run
# - interval: cada 30s verifica la salud
# - timeout: espera máx 10s por respuesta
# - start-period: da 90s para que la app inicie
# - retries: intenta 5 veces antes de marcar como "unhealthy"
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# ✅ Comando de inicio optimizado
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
