package com.quarkus.api.resource.dto;

import com.quarkus.api.domain.enums.Status;
import jakarta.validation.constraints.NotNull;

public class ServiceOrderStatusUpdateRequestDTO {

    @NotNull(message = "O status não pode ser nulo.") // Use @NotNull
    private Status status;

    public ServiceOrderStatusUpdateRequestDTO() {
    }

    public ServiceOrderStatusUpdateRequestDTO(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
