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

# Copia el jar construido (toma el único .jar del target)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
