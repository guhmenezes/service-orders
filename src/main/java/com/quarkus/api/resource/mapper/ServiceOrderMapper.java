package com.quarkus.api.resource.mapper;

import com.quarkus.api.domain.model.ServiceOrder;
import com.quarkus.api.resource.dto.ServiceOrderCreateRequestDTO;
import com.quarkus.api.resource.dto.ServiceOrderResponseDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

// @ApplicationScoped é bom se você precisar injetar outros mappers ou serviços aqui.
// Para mapeamentos simples, você pode usar métodos estáticos sem injetar o mapper.
// Vou deixar como ApplicationScoped para flexibilidade futura.
@ApplicationScoped
public class ServiceOrderMapper {

    public ServiceOrder toEntity(ServiceOrderCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        ServiceOrder entity = new ServiceOrder();
        entity.setCustomer(dto.getCustomer());
        entity.setDescription(dto.getDescription());
        return entity;
    }

    public ServiceOrderResponseDTO toDto(ServiceOrder entity) {
        if (entity == null) {
            return null;
        }
        return new ServiceOrderResponseDTO(
                entity.id,
                entity.getCustomer(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    public List<ServiceOrderResponseDTO> toDtoList(List<ServiceOrder> entities) {
        if (entities == null) {
            return List.of(); // Retorna lista vazia em vez de null
        }
        return entities.stream()
                .map(this::toDto) // Usa o método toDto singular
                .collect(Collectors.toList());
    }
}