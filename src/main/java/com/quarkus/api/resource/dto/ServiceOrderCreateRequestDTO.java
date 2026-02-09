package com.quarkus.api.resource.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ServiceOrderCreateRequestDTO {

    @NotBlank(message = "O nome do cliente não pode ser vazio")
    @Size(min = 3, max = 100, message = "O nome do cliente deve ter entre 3 e 100 caracteres")
    public String customer;

    @NotBlank(message = "A descrição não pode ser vazia")
    @Size(min = 10, max = 255, message = "A descrição deve ter entre 10 e 255 caracteres")
    public String description;

    public ServiceOrderCreateRequestDTO() {}

    public ServiceOrderCreateRequestDTO(String customer, String description) {
        this.customer = customer;
        this.description = description;
    }

    public String getCustomer() { return customer; }

    public void setCustomer(String customer) { this.customer = customer; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}