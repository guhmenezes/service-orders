package com.quarkus.api.service;

import com.quarkus.api.domain.enums.Status;
import com.quarkus.api.domain.model.ServiceOrder;
import com.quarkus.api.repository.ServiceOrderRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ServiceOrderService {

    @Inject
    ServiceOrderRepository repository;

    @Transactional
    public ServiceOrder createSO(ServiceOrder serviceOrder) {
        repository.persist(serviceOrder);
        return serviceOrder;
    }

    public List<ServiceOrder> listSO(int pageIndex, int size) {
        if (pageIndex < 0) {
            pageIndex = 0;
        }
        if (size <= 0) {
            size = 10;
        }

        Page page = Page.of(pageIndex, size);

        return repository.findAll().page(page).list();
    }

    public ServiceOrder findById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void updateStatus(Long id, Status newStatus) {
        ServiceOrder serviceOrder = findById(id);
        serviceOrder.setStatus(newStatus);
    }
}
