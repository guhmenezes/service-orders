# --- STAGE 1: BUILD THE APPLICATION ---
# Usa uma imagem Maven com JDK 17.
# A tag '3.9.6-jdk-17' é uma das mais recentes e confiáveis para Maven com JDK 17.
FROM maven:3.8.8-amazoncorretto-17 AS build

# Define o diretório de trabalho dentro do container do estágio de build.
WORKDIR /app

# Copia o arquivo pom.xml primeiro.
# Isso é uma otimização: se o pom.xml não mudar, o Docker reutiliza esta camada
# e não precisa baixar as dependências novamente, acelerando builds futuros.
COPY pom.xml .

# Baixa todas as dependências do Maven.
# -B: Modo batch (não interativo).
# -DskipTests: Evita a execução de testes nesta fase de download de dependências.
RUN mvn dependency:go-offline -B

# Copia todo o restante do código fonte da sua aplicação.
COPY src ./src

# Compila o projeto Quarkus.
# -DskipTests: Garante que os testes não serão executados durante a compilação no Docker.
# -Dquarkus.package.type=mutable-jar: ESSENCIAL para Quarkus em containers.
#   Cria uma estrutura de diretórios "quarkus-app" com o código da aplicação separado
#   das dependências, permitindo uma imagem final mais eficiente.
RUN mvn clean package -DskipTests -Dquarkus.package.type=mutable-jar

# --- STAGE 2: RUN THE APPLICATION ---
# Usa uma imagem JRE (Java Runtime Environment) slim para rodar a aplicação.
# A tag '17-slim-jre' é a tag slim correta para o JRE 17.
FROM eclipse-temurin:17-jre-focal

# Define o diretório de trabalho para o container de execução.
WORKDIR /app

# Copia os arquivos gerados no estágio de build (otimizado para mutable-jar).
COPY --from=build /app/target/quarkus-app/lib /app/lib
COPY --from=build /app/target/quarkus-app/*.jar /app/app.jar
COPY --from=build /app/target/quarkus-app/app /app/app
COPY --from=build /app/target/quarkus-app/quarkus /app/quarkus

# Expõe a porta padrão do Quarkus (8080).
# Isso é uma documentação; para acessar, você precisará mapear a porta ao rodar o container.
EXPOSE 8080

# Comando que será executado quando o container iniciar.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# --- BOAS PRÁTICAS DE SEGURANÇA (Opcional, mas recomendado) ---
# RUN groupadd -r appuser && useradd -r -g appuser appuser
# USER appuser