# Usar a imagem do JDK 21 (Java 21)
FROM eclipse-temurin:21-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o JAR gerado pelo Maven para o contêiner
COPY target/*.jar app.jar

# Expõe a porta 8080 para acesso externo
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

