package com.quarkus.api.repository;

import com.quarkus.api.domain.model.ServiceOrder;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceOrderRepository implements PanacheRepository<ServiceOrder> {

}