# Etapa de build
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o código fonte
COPY . .

# Garante permissão de execução ao wrapper do Maven
RUN chmod +x ./mvnw

# Compila o projeto sem rodar os testes
RUN ./mvnw clean package -DskipTests

# Etapa de execução
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copia o .jar gerado da fase anterior
COPY --from=build /app/target/Autofinance-0.0.1-SNAPSHOT.jar app.jar

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]