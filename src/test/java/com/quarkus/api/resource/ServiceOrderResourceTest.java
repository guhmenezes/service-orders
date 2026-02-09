package com.quarkus.api.resource;

import com.quarkus.api.domain.enums.Status;
import com.quarkus.api.domain.model.ServiceOrder;
import com.quarkus.api.resource.dto.ServiceOrderCreateRequestDTO; // NOVO
import com.quarkus.api.resource.dto.ServiceOrderStatusUpdateRequestDTO; // NOVO
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
// import static org.hamcrest.CoreMatchers.nullValue; // Não usado, pois status padrão é ABERTA
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@DisplayName("ServiceOrderResource Integration Tests")
class ServiceOrderResourceTest {

    @Inject
    private EntityManager entityManager;
    private Long osIdAberta;
    private Long osIdEmAndamento;
    private Long osIdFinalizada; // Adicione este

    @BeforeEach
    @Transactional
    void setup() {
        ServiceOrder.deleteAll();

        ServiceOrder os1 = new ServiceOrder("Cliente A", "Manutenção de Servidor", Status.ABERTA);
        os1.persist();
        osIdAberta = os1.id;

        ServiceOrder os2 = new ServiceOrder("Cliente B", "Instalação de Software", Status.EM_ANDAMENTO);
        os2.persist();
        osIdEmAndamento = os2.id;

        ServiceOrder os3 = new ServiceOrder("Cliente C", "Troca de Componentes", Status.FINALIZADA);
        os3.persist();
        osIdFinalizada = os3.id; // Guarde o ID
    }

    // Método auxiliar para criar N ServiceOrders e garantir que sejam commitadas
    // *** AGORA COM SEU PRÓPRIO @Transactional(TxType.REQUIRES_NEW) ***
    @Transactional(Transactional.TxType.REQUIRES_NEW) // Importe jakarta.transaction.Transactional
    protected void createNServiceOrders(int n) {
        for (int i = 0; i < n; i++) {
            ServiceOrder so = new ServiceOrder("Cliente Teste " + i, "Desc Teste " + i, Status.ABERTA);
            entityManager.persist(so); // Persiste usando o EntityManager
        }
        // flush é uma boa prática para garantir que os dados sejam enviados ao DB antes do commit
        entityManager.flush();
        // clear limpa o cache do EM, garantindo que leituras futuras venham do DB
        entityManager.clear();
    }

    @AfterEach
    @Transactional
    void teardown() {
        ServiceOrder.deleteAll();
    }

    // --- Testes para POST /ordens-servico ---
    @Test
    @Transactional
    @DisplayName("POST /ordens-servico - Deve criar uma nova OS com sucesso")
    void testCreateServiceOrder() {
        ServiceOrderCreateRequestDTO requestDTO = new ServiceOrderCreateRequestDTO("Cliente Teste", "Nova Ordem de Serviço Teste");

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO DE REQUEST
                .when()
                .post("/ordens-servico")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .body("customer", is("Cliente Teste"))
                .body("description", is("Nova Ordem de Serviço Teste"))
                .body("status", is("ABERTA")) // Status inicial é ABERTA por padrão
                .body("createdAt", is(notNullValue()));
        // REMOVIDO: updatedAt

        // Verifica se a OS foi realmente persistida no banco de dados
        assertEquals(4, ServiceOrder.count());
        ServiceOrder createdSoFromDb = ServiceOrder.find("description", "Nova Ordem de Serviço Teste").firstResult();
        assertNotNull(createdSoFromDb);
        assertEquals("Cliente Teste", createdSoFromDb.getCustomer());
        assertEquals(Status.ABERTA, createdSoFromDb.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("POST /ordens-servico - Deve retornar 400 se o cliente for vazio")
    void testCreateServiceOrder_invalidCustomer() {
        ServiceOrderCreateRequestDTO requestDTO = new ServiceOrderCreateRequestDTO("", "Descrição válida com mais de 10 caracteres.");

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/ordens-servico")
                .then()
                .statusCode(400)
                .body("message", is("Erros de validação na requisição."))
                .body("details.message", hasItems(
                        "O nome do cliente não pode ser vazio",
                        "O nome do cliente deve ter entre 3 e 100 caracteres"
                ))
                .body("details.field", hasItems(
                        "customer",
                        "customer"
                ));
    }

    @Test
    @Transactional
    @DisplayName("POST /ordens-servico - Deve retornar 400 se a descrição for muito curta")
    void testCreateServiceOrder_shortDescription() {
        ServiceOrderCreateRequestDTO requestDTO = new ServiceOrderCreateRequestDTO("Cliente Válido", "Curta"); // Menos de 10 caracteres

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO)
                .when()
                .post("/ordens-servico")
                .then()
                .statusCode(400)
                .body("message", is("Erros de validação na requisição."))
                .body("details[0].field", is("description"))
                .body("details[0].message", is("A descrição deve ter entre 10 e 255 caracteres"));
    }

    @Test
    @Transactional
    @DisplayName("POST /ordens-servico - Deve retornar 400 para corpo da requisição vazio")
    void testCreateServiceOrder_emptyBody() {
        given()
                .contentType(ContentType.JSON)
                .body("") // Corpo vazio
                .when()
                .post("/ordens-servico")
                .then()
                .statusCode(400)
                .body(containsString("Corpo da requisição inválido ou vazio.")); // Mensagem do ServiceOrderResource
    }


    // --- Testes para GET /ordens-servico ---
    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve listar todas as OSs com paginação padrão (page=0, size=10)")
    void testListServiceOrders_defaultPagination() {
        given()
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("[0].customer", is("Cliente A"))
                .body("[0].description", is("Manutenção de Servidor"))
                .body("[0].status", is("ABERTA"))
                .body("[0].createdAt", is(notNullValue()))
                .body("[1].customer", is("Cliente B"))
                .body("[1].description", is("Instalação de Software"))
                .body("[1].status", is("EM_ANDAMENTO"))
                .body("[1].createdAt", is(notNullValue()))
                .body("[2].customer", is("Cliente C"))
                .body("[2].description", is("Troca de Componentes"))
                .body("[2].status", is("FINALIZADA"))
                .body("[2].createdAt", is(notNullValue()));
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve listar OSs com paginação específica (page=0, size=1)")
    void testListServiceOrders_specificPagination_page0_size1() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].customer", is("Cliente A"))
                .body("[0].description", is("Manutenção de Servidor"))
                .body("[0].status", is("ABERTA"))
                .body("[0].createdAt", is(notNullValue()));
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve listar OSs com paginação específica (page=1, size=1)")
    void testListServiceOrders_specificPagination_page1_size1() {
        given()
                .queryParam("page", 1)
                .queryParam("size", 1)
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].customer", is("Cliente B"))
                .body("[0].description", is("Instalação de Software"))
                .body("[0].status", is("EM_ANDAMENTO"))
                .body("[0].createdAt", is(notNullValue()));
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve retornar lista vazia para página fora do limite")
    void testListServiceOrders_pageOutOfBound() {
        given()
                .queryParam("page", 100)
                .queryParam("size", 10)
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve usar tamanho padrão se size for inválido (e.g., 0)")
    void testListServiceOrders_invalidSizeUsesDefault() {
        createNServiceOrders(7); // Adiciona 7, totalizando 10 (3 + 7)

        given()
                .queryParam("page", 0)
                .queryParam("size", 0) // Tamanho inválido, o serviço deve usar o default 10
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(10)); // Esperamos 10 itens
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico - Deve usar página 0 se page for inválido (e.g., -1)")
    void testListServiceOrders_invalidPageUsesDefault() {
        given()
                .queryParam("page", -1)
                .queryParam("size", 1)
                .when()
                .get("/ordens-servico")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].customer", is("Cliente A"));
    }

    // --- Testes para GET /ordens-servico/{id} ---
    @Test
    @Transactional
    @DisplayName("GET /ordens-servico/{id} - Deve retornar OS quando encontrada")
    void testFindById_found() {
        given()
                .pathParam("id", osIdAberta)
                .when()
                .get("/ordens-servico/{id}")
                .then()
                .statusCode(200)
                .body("id", is(osIdAberta.intValue()))
                .body("customer", is("Cliente A"))
                .body("description", is("Manutenção de Servidor"))
                .body("status", is("ABERTA"))
                .body("createdAt", is(notNullValue()));
    }

    @Test
    @Transactional
    @DisplayName("GET /ordens-servico/{id} - Deve retornar 404 quando OS não encontrada")
    void testFindById_notFound() {
        given()
                .pathParam("id", 9999L)
                .when()
                .get("/ordens-servico/{id}")
                .then()
                .statusCode(404);
    }

    // --- Testes para PUT /ordens-servico/{id}/status ---
    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve atualizar status de ABERTA para EM_ANDAMENTO")
    void testUpdateStatus_validTransition_AbertaToEmAndamento() {
        ServiceOrderStatusUpdateRequestDTO requestDTO = new ServiceOrderStatusUpdateRequestDTO(Status.EM_ANDAMENTO);

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO DE REQUEST
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(204); // No Content

        // Verifica no banco de dados se o status foi atualizado
        ServiceOrder updatedSO = ServiceOrder.findById(osIdAberta);
        assertNotNull(updatedSO);
        assertEquals(Status.EM_ANDAMENTO, updatedSO.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve atualizar status de EM_ANDAMENTO para FINALIZADA")
    void testUpdateStatus_validTransition_EmAndamentoToFinalizada() {
        ServiceOrderStatusUpdateRequestDTO requestDTO = new ServiceOrderStatusUpdateRequestDTO(Status.FINALIZADA);

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO DE REQUEST
                .pathParam("id", osIdEmAndamento)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(204);

        ServiceOrder updatedSO = ServiceOrder.findById(osIdEmAndamento);
        assertNotNull(updatedSO);
        assertEquals(Status.FINALIZADA, updatedSO.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 400 para transição inválida (ABERTA para FINALIZADA)")
    void testUpdateStatus_invalidTransition_AbertaToFinalizada() {
        ServiceOrderStatusUpdateRequestDTO requestDTO = new ServiceOrderStatusUpdateRequestDTO(Status.FINALIZADA);

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO DE REQUEST
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(400)
                .body(is("Transição de status inválida"));

        // Verifica que o status não foi alterado no DB
        ServiceOrder originalSO = ServiceOrder.findById(osIdAberta);
        assertNotNull(originalSO);
        assertEquals(Status.ABERTA, originalSO.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 400 para OS não encontrada")
    void testUpdateStatus_soNotFound() {
        ServiceOrderStatusUpdateRequestDTO requestDTO = new ServiceOrderStatusUpdateRequestDTO(Status.EM_ANDAMENTO);

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO DE REQUEST
                .pathParam("id", 9999L)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(400)
                .body(is("OS não encontrada"));
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 400 para status inválido (enum não existente)")
    void testUpdateStatus_invalidStatusEnum() {
        // Enviar uma string que não é um Status válido diretamente no corpo
        // Isso simula o cenário onde o JSON enviado é {"status": "STATUS_INEXISTENTE"}
        given()
                .contentType(ContentType.JSON)
                .body("{\"status\": \"STATUS_INEXISTENTE\"}")
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(400)
                .body("status", is(400))
                .body("error", is("Bad Request"))
                .body("message", is("Valor inválido para campo de enum: STATUS_INEXISTENTE")) // Mensagem principal
                .body("details[0].detail", is("Valor inválido para campo de enum: STATUS_INEXISTENTE"));
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 400 para status nulo (validação @NotNull)")
    void testUpdateStatus_nullStatus() {
        ServiceOrderStatusUpdateRequestDTO requestDTO = new ServiceOrderStatusUpdateRequestDTO(null); // Status nulo

        given()
                .contentType(ContentType.JSON)
                .body(requestDTO) // ENVIANDO DTO COM STATUS NULO
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(400)
                .body("message", is("Erros de validação na requisição."))
                .body("details[0].field", is("status"))
                .body("details[0].message", is("O status não pode ser nulo."));
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 400 para corpo da requisição vazio")
    void testUpdateStatus_emptyBody() {
        given()
                .contentType(ContentType.JSON)
                .body("") // Corpo vazio
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(400)
                .body(containsString("Corpo da requisição inválido ou vazio.")); // Mensagem do ServiceOrderResource
    }

    @Test
    @Transactional
    @DisplayName("PUT /ordens-servico/{id}/status - Deve retornar 415 para corpo da requisição não-JSON")
    void testUpdateStatus_nonJsonBody() {
        given()
                .contentType(ContentType.TEXT) // Content-Type incorreto
                .body("EM_ANDAMENTO") // Conteúdo não é JSON
                .pathParam("id", osIdAberta)
                .when()
                .put("/ordens-servico/{id}/status")
                .then()
                .statusCode(415); // Unsupported Media Type
    }
}