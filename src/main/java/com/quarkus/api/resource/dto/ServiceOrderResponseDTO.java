package com.quarkus.api.resource.dto;

import com.quarkus.api.domain.enums.Status;
import com.quarkus.api.domain.model.ServiceOrder;

import java.time.LocalDateTime;

public class ServiceOrderResponseDTO {
    public Long id;
    public String customer;
    public String description;
    public Status status;
    public LocalDateTime createdAt;

    public ServiceOrderResponseDTO() {}

    public ServiceOrderResponseDTO(Long id, String customer, String description, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.customer = customer;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static ServiceOrderResponseDTO fromEntity(ServiceOrder entity) {
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

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCustomer() { return customer; }

    public void setCustomer(String customer) { this.customer = customer; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}