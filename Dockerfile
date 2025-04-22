# Etapa de build
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o código fonte
COPY . .

# Garante permissão de execução ao wrapper do Maven
RUN chmod +x ./mvnw

# Executa o build, sem testes
RUN ./mvnw clean package -DskipTests

# Etapa de execução
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copia o .jar gerado do build
COPY --from=build /app/target/Autofinance-0.0.1-SNAPSHOT.jar.original app.jar

# Comando para rodar a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]