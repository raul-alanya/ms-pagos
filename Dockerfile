# Etapa 1: Compilar el proyecto y generar el WAR
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Correr el WAR con Java (Tomcat embebido incluido)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el WAR generado (ejecutable con "java -jar")
COPY --from=build /app/target/ms-pagos-0.0.1.war app.war

# Puerto expuesto
EXPOSE 8080

# Comando para ejecutar
ENTRYPOINT ["java", "-jar", "app.war"]
