# 🚀 API de Gerenciamento de Ordens de Serviço (Quarkus)

## 📝 Descrição do Projeto

Este projeto consiste em uma API RESTful para o gerenciamento de Ordens de Serviço (OS), desenvolvida em **Java 17** utilizando o framework **Quarkus**. O objetivo é demonstrar a construção de uma aplicação backend moderna, aplicando conceitos como REST, persistência de dados com JPA/Hibernate, gerenciamento de esquema com Liquibase, testes robustos e conteinerização com Docker.

O foco principal é na implementação de funcionalidades de CRUD (Create, Read, Update) para Ordens de Serviço, incluindo uma regra de negócio específica para transições de status, e a aderência a práticas corporativas de desenvolvimento.

## ✨ Funcionalidades

A API oferece os seguintes endpoints para gerenciamento de Ordens de Serviço:

*   **Criar OS:** `POST /ordens-servico`
    *   Cliente e descrição são campos obrigatórios (`@NotBlank`).
    *   Status inicial é `ABERTA`.
    *   `creationDate` é definida no momento da criação.
*   **Listar OS:** `GET /ordens-servico`
    *   Suporte a paginação (`page`, `size`).
*   **Buscar OS por ID:** `GET /ordens-servico/{id}`
    *   Retorna `404 Not Found` se a OS não existir.
*   **Atualizar Status da OS:** `PUT /ordens-servico/{id}/status`
    *   Regras de transição de status:
        *   `ABERTA` → `EM_ANDAMENTO`
        *   `EM_ANDAMENTO` → `FINALIZADA`
    *   Qualquer outra transição resulta em erro (`400 Bad Request`).

## 🛠️ Tecnologias Utilizadas (Stack)

*   **Linguagem:** Java 17
*   **Framework:** Quarkus (versão 3.3.1)
*   **Persistência:**
    *   JPA / Hibernate ORM com Panache
    *   Banco de Dados: H2 (em memória, simulando banco relacional)
    *   Gerenciamento de Schema: Liquibase
*   **Web:** REST (RESTEasy Reactive com Jackson)
*   **Testes:**
    *   JUnit 5
    *   Mockito (para testes unitários)
    *   RestAssured (para testes de integração)
*   **Build Tool:** Apache Maven
*   **Controle de Versão:** Git
*   **Conteinerização:** Docker
*   **Validação:** Hibernate Validator

## 🏛️ Arquitetura e Estrutura de Pacotes

O projeto segue uma arquitetura em camadas com uma estrutura de pacotes modular, visando a separação de responsabilidades e a manutenibilidade. A organização dos pacotes sob `src/main/java/com/example/serviceorders/` é a seguinte:

```bash
src/main/java/com/example/serviceorders/
├── domain/ # Camada de Domínio: Contém as entidades (OrdemServico), enums (StatusOrdemServico) e exceções de negócio (NotFoundException, InvalidStatusTransitionException). Representa o núcleo da lógica de negócio.
├── dto/ # Data Transfer Objects (DTOs): Objetos utilizados para a comunicação da API (OrdemServicoRequestDTO, OrdemServicoResponseDTO, PagedResponse, StatusUpdateRequestDTO), desacoplando o modelo de domínio do contrato da API.
├── mapper/ # Mapeadores: Classes responsáveis pela conversão bidirecional entre entidades de domínio e seus respectivos DTOs (OrdemServicoMapper).
├── repository/ # Camada de Repositório: Fornece a abstração para o acesso a dados, utilizando Panache para interagir com o banco de dados (OrdemServicoRepository).
├── resource/ # Camada de Recurso (Controller): Endpoints REST da API, responsáveis por receber as requisições HTTP, validar os DTOs de entrada e delegar as chamadas para a camada de serviço (OrdemServicoResource).
└── service/ # Camada de Serviço: Contém a lógica de negócio principal da aplicação. Orquestra as operações, aplica as regras de negócio (como a transição de status) e coordena o acesso a dados através do repositório (OrdemServicoService).
```
Essa organização promove a separação de preocupações, tornando o código mais legível, testável e fácil de manter, ao mesmo tempo em que aproveita a simplicidade e a produtividade do Quarkus.

## ⚙️ Como Rodar o Projeto

### Pré-requisitos

*   Java Development Kit (JDK) 17 ou superior
*   Apache Maven 3.8.x ou superior
*   Docker Desktop (ou ambiente Docker)

### 💻 Executar Localmente (Modo Desenvolvimento)

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/guhmenezes/service-orders.git
    cd service-orders
    ```
2.  **Inicie a aplicação em modo de desenvolvimento:**
    ```bash
    mvn quarkus:dev
    ```
    A aplicação estará disponível em `http://localhost:8080`.

3.  **Acessar Ferramentas de Desenvolvimento (apenas em `dev` profile):**
    *   **Quarkus Dev UI:** `http://localhost:8080/q/dev-ui` (para monitoramento e insights do Quarkus)
    *   **Swagger UI (OpenAPI):** `http://localhost:8080/swagger-ui` (para documentação interativa da API)
    *   **H2 Console:** `http://localhost:8080/h2-console` (credenciais: `sa`/` ` - sem senha)

### 🐳 Executar via Docker

1.  **Certifique-se de que o Docker está rodando.**
2.  **Navegue até a raiz do projeto** (onde o `Dockerfile` está).
3.  **Construa a imagem Docker:**
    ```bash
    docker build -t service-orders-api:1.0.0 .
    ```
4.  **Execute o container Docker:**
    ```bash
    docker run -i --rm -p 8080:8080 service-orders-api:1.0.0
    ```
    A aplicação estará disponível em `http://localhost:8080`.
    *(Note que ao rodar via Docker, a aplicação estará no perfil `prod`. O Swagger UI, H2 Console e Dev UI não estarão disponíveis por padrão para otimização e segurança em produção).*

## 🧪 Testes

O projeto possui cobertura de testes unitários e de integração para garantir a correção das funcionalidades e regras de negócio.

*   **Testes Unitários:** Focam na lógica da camada de `Service`, utilizando JUnit 5 e Mockito para isolar as dependências. Cobrem especialmente as regras de transição de status.
*   **Testes de Integração:** Validam o comportamento dos endpoints REST, utilizando `QuarkusTest` e `RestAssured` para simular requisições HTTP e verificar as respostas da API, incluindo códigos de status e conteúdo.

Para executar todos os testes:

```bash
mvn test
```

## 📚 Endpoints da API

A documentação interativa completa de todos os endpoints da API, incluindo exemplos de requisição e resposta, pode ser acessada através do **Swagger UI**.

Para acessar o Swagger UI:

1.  **Execute a aplicação em modo de desenvolvimento:**
    ```bash
    mvn quarkus:dev
    ```
2.  **Abra seu navegador e acesse:**
    `http://localhost:8080/swagger-ui`

Isso permitirá que você explore e teste todos os endpoints da API de forma intuitiva.


## 📝 Histórico de Commits (Git)

O histórico de commits foi estruturado de forma incremental, refletindo as etapas de desenvolvimento e as boas práticas de versionamento:

*   `feat: Initialize Quarkus project structure with core dependencies`
*   `feat: Implement Liquibase for database schema management and initial table creation`
*   `feat: Implement OrdemServico entity, repository, DTOs and mappers`
*   `feat: Implement basic CRUD for Ordem de Serviço (Create, List, Get by ID)`
*   `feat: Implement business rule for Ordem de Serviço status update`
*   `test: Add unit and integration tests for Ordem de Serviço`
*   `build: Add Dockerfile for application containerization`

Esta abordagem demonstra um processo de desenvolvimento organizado e facilita a revisão do código e a identificação de mudanças.

## 💡 Diferenciais Conceituais

Este projeto foi desenvolvido com Quarkus, que é um diferencial em si. Além disso, as seguintes tecnologias seriam integrações naturais e benéficas em um ambiente de produção real, demonstrando visão de arquitetura e ecossistema:

### 🌐 OpenShift (ou Kubernetes)

*   **Deployment e Escala:** A aplicação seria implantada em um cluster OpenShift/Kubernetes, permitindo gerenciamento de Pods, escalabilidade horizontal automática (HPA) baseada em métricas (CPU, requisições), e alta disponibilidade.
*   **Gerenciamento de Configuração e Segredos:** Utilização de `ConfigMaps` para configurações específicas de ambiente e `Secrets` para dados sensíveis (senhas de banco de dados), desacoplando a configuração do código da aplicação.
*   **Observabilidade:** Integração com as ferramentas de monitoramento (Prometheus/Grafana) e centralização de logs (ELK Stack) do cluster para visibilidade completa da aplicação.
*   **CI/CD:** Integração com pipelines de CI/CD para automação do build, teste e deploy da imagem Docker no cluster.

### 🔍 Elasticsearch

*   **Centralização de Logs (ELK Stack):** Os logs da aplicação seriam enviados para o Elasticsearch (via Logstash ou Fluent Bit) para indexação e análise. O Kibana seria utilizado para visualização, busca e criação de dashboards operacionais, facilitando a depuração e o monitoramento em produção.
*   **Busca Avançada de Ordens de Serviço:** Para um volume maior de dados ou requisitos de busca textual complexos (fuzzy search, pesquisa por múltiplos campos, etc.), o Elasticsearch poderia ser utilizado como um motor de busca secundário. As Ordens de Serviço seriam indexadas no Elasticsearch após serem persistidas no banco de dados relacional, permitindo consultas otimizadas para busca e agregação de dados.
