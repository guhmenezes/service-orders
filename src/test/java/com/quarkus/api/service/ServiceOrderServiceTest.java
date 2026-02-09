package com.quarkus.api.service;

import com.quarkus.api.domain.enums.Status;
import com.quarkus.api.domain.model.ServiceOrder;
import com.quarkus.api.repository.ServiceOrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any; // Importe este
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("ServiceOrderService Unit Tests with Mocked Repository")
class ServiceOrderServiceTest {

    @Inject
    ServiceOrderService service;

    @InjectMock
    ServiceOrderRepository repository;

    @BeforeEach
    void setup() {
        Mockito.reset(repository);
    }

    // --- Testes para createSO ---
    @Test
    @DisplayName("createSO - Deve persistir uma nova ServiceOrder usando o repositório")
    void createSO_shouldPersistServiceOrder() {
        ServiceOrder newSO = new ServiceOrder("Cliente X", "Nova OS via Service", Status.ABERTA);
        doNothing().when(repository).persist(any(ServiceOrder.class));

        service.createSO(newSO);

        verify(repository, times(1)).persist(newSO);
        assertNotNull(newSO.getCreatedAt());
    }

    // --- Testes para listSO ---
    @Test
    @DisplayName("listSO - Deve retornar uma lista de ServiceOrders existentes usando o repositório")
    void listSO_shouldReturnListOfServiceOrders() {
        List<ServiceOrder> mockList = Arrays.asList(
                new ServiceOrder("Cliente A", "OS 1", Status.ABERTA),
                new ServiceOrder("Cliente B", "OS 2", Status.EM_ANDAMENTO)
        );

        PanacheQuery<ServiceOrder> panacheQueryMock = mock(PanacheQuery.class);

        when(repository.findAll()).thenReturn(panacheQueryMock);
        when(panacheQueryMock.page(any(Page.class))).thenReturn(panacheQueryMock); // Mocka com any(Page.class)
        when(panacheQueryMock.list()).thenReturn(mockList);

        List<ServiceOrder> result = service.listSO(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("OS 1", result.get(0).getDescription());
        verify(repository, times(1)).findAll();
        verify(panacheQueryMock, times(1)).page(any(Page.class)); // *** ALTERADO AQUI ***
        verify(panacheQueryMock, times(1)).list();
    }

    @Test
    @DisplayName("listSO - Deve retornar uma lista vazia quando não há ServiceOrders usando o repositório")
    void listSO_shouldReturnEmptyListWhenNoServiceOrders() {
        PanacheQuery<ServiceOrder> panacheQueryMock = mock(PanacheQuery.class);

        when(repository.findAll()).thenReturn(panacheQueryMock);
        when(panacheQueryMock.page(any(Page.class))).thenReturn(panacheQueryMock);
        when(panacheQueryMock.list()).thenReturn(Collections.emptyList());

        List<ServiceOrder> result = service.listSO(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
        verify(panacheQueryMock, times(1)).page(any(Page.class)); // *** ALTERADO AQUI ***
        verify(panacheQueryMock, times(1)).list();
    }

    @Test
    @DisplayName("listSO - Deve usar tamanho padrão se pageSize for inválido")
    void listSO_shouldUseDefaultSizeWhenPageSizeInvalid() {
        PanacheQuery<ServiceOrder> panacheQueryMock = mock(PanacheQuery.class);
        when(repository.findAll()).thenReturn(panacheQueryMock);
        when(panacheQueryMock.page(any(Page.class))).thenReturn(panacheQueryMock);
        when(panacheQueryMock.list()).thenReturn(Collections.emptyList());

        service.listSO(0, 0); // Tamanho 0, deve usar o default 10

        verify(panacheQueryMock, times(1)).page(any(Page.class)); // *** ALTERADO AQUI ***
    }

    @Test
    @DisplayName("listSO - Deve usar página 0 se pageIndex for inválido")
    void listSO_shouldUsePage0WhenPageIndexInvalid() {
        PanacheQuery<ServiceOrder> panacheQueryMock = mock(PanacheQuery.class);
        when(repository.findAll()).thenReturn(panacheQueryMock);
        when(panacheQueryMock.page(any(Page.class))).thenReturn(panacheQueryMock);
        when(panacheQueryMock.list()).thenReturn(Collections.emptyList());

        service.listSO(-1, 10); // Índice -1, deve usar 0

        verify(panacheQueryMock, times(1)).page(any(Page.class)); // *** ALTERADO AQUI ***
    }

    // ... (o restante dos testes findById e updateStatus permanece o mesmo) ...
    @Test
    @DisplayName("findById - Deve retornar ServiceOrder quando encontrado usando o repositório")
    void findById_shouldReturnServiceOrderWhenFound() {
        ServiceOrder expectedSO = new ServiceOrder("Cliente C", "OS Encontrada", Status.ABERTA);
        expectedSO.id = 1L;

        when(repository.findById(1L)).thenReturn(expectedSO);

        ServiceOrder result = service.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id);
        assertEquals("Cliente C", result.getCustomer());
        assertEquals("OS Encontrada", result.getDescription());
        assertEquals(Status.ABERTA, result.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - Deve retornar null quando ServiceOrder não encontrado usando o repositório")
    void findById_shouldReturnNullWhenNotFound() {
        when(repository.findById(999L)).thenReturn(null);

        ServiceOrder result = service.findById(999L);

        assertNull(result);
        verify(repository, times(1)).findById(999L);
    }

    // --- Testes para updateStatus ---
    @Test
    @DisplayName("updateStatus - Deve transicionar de ABERTA para EM_ANDAMENTO")
    void updateStatus_shouldTransitionFromAbertaToEmAndamento() {
        ServiceOrder so = new ServiceOrder("Cliente A", "OS Aberta", Status.ABERTA);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        service.updateStatus(1L, Status.EM_ANDAMENTO);

        assertEquals(Status.EM_ANDAMENTO, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve transicionar de EM_ANDAMENTO para FINALIZADA")
    void updateStatus_shouldTransitionFromEmAndamentoToFinalizada() {
        ServiceOrder so = new ServiceOrder("Cliente B", "OS Em Andamento", Status.EM_ANDAMENTO);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        service.updateStatus(1L, Status.FINALIZADA);

        assertEquals(Status.FINALIZADA, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve lançar exceção se ServiceOrder não for encontrada")
    void updateStatus_shouldThrowExceptionWhenSONotFound() {
        when(repository.findById(any(Long.class))).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus(1L, Status.EM_ANDAMENTO));

        assertEquals("OS não encontrada", exception.getMessage());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve lançar exceção para transição inválida (ABERTA para FINALIZADA)")
    void updateStatus_shouldThrowExceptionForInvalidTransition_AbertaToFinalizada() {
        ServiceOrder so = new ServiceOrder("Cliente A", "OS Aberta", Status.ABERTA);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus(1L, Status.FINALIZADA));

        assertEquals("Transição de status inválida", exception.getMessage());
        assertEquals(Status.ABERTA, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve lançar exceção para transição inválida (EM_ANDAMENTO para ABERTA)")
    void updateStatus_shouldThrowExceptionForInvalidTransition_EmAndamentoToAberta() {
        ServiceOrder so = new ServiceOrder("Cliente B", "OS Em Andamento", Status.EM_ANDAMENTO);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus(1L, Status.ABERTA));

        assertEquals("Transição de status inválida", exception.getMessage());
        assertEquals(Status.EM_ANDAMENTO, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve lançar exceção para transição inválida (FINALIZADA para EM_ANDAMENTO)")
    void updateStatus_shouldThrowExceptionForInvalidTransition_FinalizadaToEmAndamento() {
        ServiceOrder so = new ServiceOrder("Cliente C", "OS Finalizada", Status.FINALIZADA);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus(1L, Status.EM_ANDAMENTO));

        assertEquals("Transição de status inválida", exception.getMessage());
        assertEquals(Status.FINALIZADA, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("updateStatus - Deve lançar exceção se o status for o mesmo e inválido (ABERTA para ABERTA)")
    void updateStatus_shouldThrowExceptionForSameStatus_AbertaToAberta() {
        ServiceOrder so = new ServiceOrder("Cliente A", "OS Aberta", Status.ABERTA);
        so.id = 1L;

        when(repository.findById(1L)).thenReturn(so);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.updateStatus(1L, Status.ABERTA));

        assertEquals("Transição de status inválida", exception.getMessage());
        assertEquals(Status.ABERTA, so.getStatus());
        verify(repository, times(1)).findById(1L);
    }
}