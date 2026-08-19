# Etapa de compilação
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copia o projeto e compila o JAR dentro do Docker
COPY . .
RUN chmod +x mvnw && ./mvnw package -DskipTests

# Etapa de execução
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080 para acesso externo
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

